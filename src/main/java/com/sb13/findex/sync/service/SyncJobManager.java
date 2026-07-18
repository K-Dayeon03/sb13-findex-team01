package com.sb13.findex.sync.service;


import com.sb13.findex.global.config.ExternalApiProperties;
import com.sb13.findex.indexdata.dto.command.IndexDataOpenApiCommand;
import com.sb13.findex.indexinfo.dto.command.IndexInfoCreateCommand;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.indexinfo.service.IndexInfoReader;
import com.sb13.findex.sync.client.model.OpenApiErrorCode;
import com.sb13.findex.sync.dto.command.IndexDataSyncCommand;
import com.sb13.findex.sync.dto.command.IndexInfoKey;
import com.sb13.findex.sync.dto.request.StockMarketIndexApiRequest;
import com.sb13.findex.sync.dto.response.DataGoKrApiResponse;
import com.sb13.findex.sync.dto.response.StockMarketIndex;
import com.sb13.findex.sync.dto.response.SyncJobDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncJobManager {

    private static final String SUCCESS_CODE = "00";
    private static final int MAX_FETCH_BATCH_SIZE = 50;
    private static final int DEFAULT_NUM_OF_ROWS = 5_000;
    private static final int DEFAULT_PAGE_NUMBER = 1;

    private final DataGoKrApiService dataGoKrApiService;

    private final IpAddressService ipAddressService;

    private final Executor externalApiExecutor;

    private final SyncJobService syncJobService;

    private final IndexInfoReader indexInfoReader;

    private final ExternalApiProperties externalApiProperties;

    public List<SyncJobDto> syncIndexInfos() {
        StockMarketIndexApiRequest request = StockMarketIndexApiRequest.ofPage(DEFAULT_NUM_OF_ROWS, DEFAULT_PAGE_NUMBER);

        DataGoKrApiResponse<StockMarketIndex> firstResponse =
                dataGoKrApiService.getStockMarketIndexList(request);

        List<StockMarketIndex> items =
                fetchPagesAsync(firstResponse, request);

        if (items.isEmpty()) {
            log.warn("동기화할 주가지수 정보가 없습니다.");
            return List.of();
        }

        Map<IndexInfoKey, StockMarketIndex> latestIndexMap =
                getLatestStockMarketIndices(items);

        List<IndexInfoCreateCommand> infoCreateCommands =
                latestIndexMap.values()
                        .stream()
                        .map(StockMarketIndex::toIndexInfoCommand)
                        .toList();

        String worker = ipAddressService.getClientIp();

        return syncJobService.indexInfoSaveAll(infoCreateCommands, worker);

    }

    public List<SyncJobDto> syncIndexDataList(
            List<IndexDataSyncCommand> commands,
            String worker
    ) {
        boolean isSystemWorker = "system".equals(worker);
        Optional<UUID> uuid = executeSync(commands, worker);

        if (isSystemWorker) {
            // 대량의 데이터가 있을경우 오류가 발생하는 것.. 같음..
            // 좀더 테스트 필요.
            return List.of();
        }

        return uuid
                .map(syncJobService::foundSyncJobs)
                .orElseGet(List::of);
    }

    private Optional<UUID> executeSync(
            List<IndexDataSyncCommand> commands,
            String worker
    ) {
        List<Long> indexInfoIds = commands.stream().map(IndexDataSyncCommand::indexInfoId).toList();

        List<IndexInfo> indexInfos = indexInfoReader.findIndexInfosByIds(indexInfoIds);
        if (indexInfos.isEmpty()) {
            log.warn("동기화할 지수정보가 없습니다.");
            return Optional.empty();
        }

        Map<Long, IndexDataSyncCommand> indexDataSyncMap = commands.stream()
                .collect(Collectors.toMap(IndexDataSyncCommand::indexInfoId, Function.identity()));

        List<StockIndexFetchTarget> fetchTargets = createFetchTargets(indexInfos, indexDataSyncMap);

        UUID uuid = UUID.randomUUID();
        fetchInBatches(fetchTargets,worker, uuid);
        return Optional.of(uuid);
    }

    public List<SyncJobDto> syncIndexDataList(List<IndexDataSyncCommand> commands) {
        return syncIndexDataList(commands, ipAddressService.getClientIp());
    }

    private List<StockMarketIndex> fetchPagesAsync(DataGoKrApiResponse<StockMarketIndex> firstResponse, StockMarketIndexApiRequest request) {
        validateFirstResponse(firstResponse);

        Integer totalPages = firstResponse.getTotalPages();
        Integer pageNo = firstResponse.getPageNo();
        Integer numOfRows = firstResponse.getNumOfRows();

        validatePageInformation(
                totalPages,
                pageNo,
                numOfRows
        );

        int fetchBatchSize = resolveFetchBatchSize();

        Map<IndexInfoKey, StockMarketIndex> latestStockMarketIndices = getLatestStockMarketIndices(firstResponse.getItem());

        for (int fromPage = pageNo + 1; fromPage <= totalPages; fromPage += fetchBatchSize) {

            int toPage = Math.min(
                    fromPage + fetchBatchSize - 1,
                    totalPages
            );

            List<CompletableFuture<PageFetchOutcome>> futures =
                    IntStream.rangeClosed(fromPage, toPage)
                            .mapToObj(currentPage -> {
                                StockMarketIndexApiRequest pageRequest =
                                        request.withPageNo(currentPage)
                                                .withNumOfRows(numOfRows);

                                return fetchPageAsync(
                                        currentPage,
                                        pageRequest
                                );
                            })
                            .toList();

            CompletableFuture.allOf(
                    futures.toArray(CompletableFuture[]::new)
            ).join();

            futures.stream()
                    .map(CompletableFuture::join)
                    .filter(outcome -> {
                        if (!outcome.isSuccess()) {
                            log.error(
                                    "지수 정보 페이지 조회 실패. page={}",
                                    outcome.pageNo(),
                                    outcome.error()
                            );
                        }
                        return outcome.isSuccess();
                    })
                    .flatMap(outcome -> outcome.items.stream())
                    .forEach(current->{
                        IndexInfoKey infoKey = new IndexInfoKey(current.idxCsf(), current.idxNm());

                        latestStockMarketIndices.merge(
                                infoKey,
                                current,
                                this::selectLatest
                        );
                    });

        }

        return List.copyOf(latestStockMarketIndices.values());
    }

    private CompletableFuture<PageFetchOutcome> fetchPageAsync(int pageNo, StockMarketIndexApiRequest request) {
        try {
            return CompletableFuture
                    .supplyAsync(
                            () -> fetchPage(pageNo, request),
                            externalApiExecutor
                    )
                    .handle((items, throwable) -> {
                        if (throwable == null) {
                            return PageFetchOutcome.success(
                                    pageNo,
                                    items
                            );
                        }

                        return PageFetchOutcome.failure(
                                pageNo,
                                unwrap(throwable)
                        );
                    });

        } catch (RejectedExecutionException exception) {
            return CompletableFuture.completedFuture(
                    PageFetchOutcome.failure(
                            pageNo,
                            exception
                    )
            );
        }
    }

    private List<StockMarketIndex> fetchPage(
            int pageNo,
            StockMarketIndexApiRequest request
    ) {
        DataGoKrApiResponse<StockMarketIndex> response =
                dataGoKrApiService.getStockMarketIndexList(request);

        if (isResponseError(response)) {
            throw new IllegalStateException(
                    "지수 정보 페이지 조회 실패. page=" + pageNo
            );
        }

        return response.getItem();
    }

    private void validateFirstResponse(
            DataGoKrApiResponse<StockMarketIndex> response
    ) {
        if (isResponseError(response)) {
            throw new IllegalStateException(
                    "주가지수 첫 페이지 조회에 실패했습니다."
            );
        }
    }

    private void validatePageInformation(
            Integer totalPages,
            Integer pageNo,
            Integer numOfRows
    ) {
        if (totalPages == null
                || pageNo == null
                || numOfRows == null
                || numOfRows <= 0) {
            throw new IllegalStateException(
                    "주가지수 API 페이지 정보가 올바르지 않습니다. "
                            + "pageNo=" + pageNo
                            + ", numOfRows=" + numOfRows
                            + ", totalPages=" + totalPages
            );
        }
    }


    private <T> boolean isResponseError(DataGoKrApiResponse<T> response) {
        boolean hasError = !SUCCESS_CODE.equals(response.getResultCode());
        if (hasError) {
            log.error("response : {}", response);
            OpenApiErrorCode apiErrorCode = OpenApiErrorCode.from(response.getResultCode());
            log.error("errorMessage : {}", apiErrorCode.getMessage());
        }
        return hasError;
    }

    private Map<IndexInfoKey, StockMarketIndex> getLatestStockMarketIndices(List<StockMarketIndex> stockMarketIndexList) {
        return stockMarketIndexList.stream()
                .collect(Collectors.toMap(
                        IndexInfoKey::from,
                        Function.identity(),
                        this::selectLatest
                ));
    }

    private StockMarketIndex selectLatest(StockMarketIndex exist, StockMarketIndex incoming) {
        LocalDate existDate = exist.parseBasDt();
        LocalDate incomingDate = incoming.parseBasDt();
        if (existDate == null) {
            return incomingDate == null ? exist : incoming;
        }

        if (incomingDate == null) {
            return exist;
        }

        return existDate.isAfter(incomingDate) ? exist : incoming;
    }

    private List<StockMarketIndex> fetchStockMarketIndexes(
            DataGoKrApiResponse<StockMarketIndex> firstResponse,
            StockMarketIndexApiRequest originRequest) {
        validateFirstResponse(firstResponse);

        Integer totalPages = firstResponse.getTotalPages();
        Integer pageNo = firstResponse.getPageNo();
        Integer numOfRows = firstResponse.getNumOfRows();

        validatePageInformation(
                totalPages,
                pageNo,
                numOfRows
        );

        List<StockMarketIndex> lists = new ArrayList<>(firstResponse.getItem());
        for (int currentPage = pageNo + 1; currentPage <= totalPages; currentPage++) {

            StockMarketIndexApiRequest pageRequest = originRequest
                    .withPageNo(currentPage)
                    .withNumOfRows(numOfRows);

            DataGoKrApiResponse<StockMarketIndex> pageResponse = dataGoKrApiService.getStockMarketIndexList(pageRequest);

            if (isResponseError(pageResponse)) {
                throw new IllegalStateException(
                        "주가지수 페이지 조회에 실패했습니다. page="
                                + currentPage
                );
            }
            lists.addAll(pageResponse.getItem());
        }
        return lists;
    }

    private IndexInfoKey createIndexInfoKey(IndexInfo info) {
        return new IndexInfoKey(info.getIndexClassification(), info.getIndexName());
    }

    /**
     * IndexInfo를 외부 API 호출 대상으로 변환합니다.
     * <p>
     * 요청 객체와 IndexInfoKey, IndexInfo를 하나의 record로 유지하므로
     * 비동기 결과가 어떤 지수에 대한 것인지 추적할 수 있습니다.
     */
    private List<StockIndexFetchTarget> createFetchTargets(
            List<IndexInfo> indexInfos, Map<Long, IndexDataSyncCommand> indexDataSyncMap
    ) {
        return indexInfos.stream()
                .map(indexInfo -> {
                    IndexInfoKey key =
                            createIndexInfoKey(indexInfo);

                    IndexDataSyncCommand indexDataSyncCommand = indexDataSyncMap.get(indexInfo.getId());

                    StockMarketIndexApiRequest request =
                            StockMarketIndexApiRequest
                                    .ofExactIndexNamePage(
                                            DEFAULT_NUM_OF_ROWS,
                                            DEFAULT_PAGE_NUMBER,
                                            indexDataSyncCommand.baseDateFrom(),
                                            key.indexName()
                                    );

                    return new StockIndexFetchTarget(
                            indexInfo,
                            key,
                            request
                    );
                })
                .toList();
    }

    /**
     * 전체 대상을 최대 50건씩 나누어 실행합니다.
     */
    private void fetchInBatches(List<StockIndexFetchTarget> targets, String worker,  UUID uuid) {
        int fetchBatchSize = resolveFetchBatchSize();


        for (int fromIndex = 0; fromIndex <= targets.size(); fromIndex += fetchBatchSize) {

            int toIndex = Math.min(fromIndex + fetchBatchSize, targets.size());

            List<StockIndexFetchTarget> batch = targets.subList(fromIndex, toIndex);

            List<IndexDataOpenApiCommand> openApiCommands = fetchBatch(batch).stream()
                    .filter(fo -> {
                        if (fo.isSuccess()) {
                            return true;
                        }
                        log.error(
                                "지수 데이터 외부 API 조회 실패. indexInfoId={}, key={}",
                                fo.target().indexInfo().getId(),
                                fo.target().key(),
                                fo.error()
                        );
                        return false;
                    })
                    .map(fo -> createIndexDataCommands(fo.target(), fo.items()))
                    .flatMap(Collection::stream)
                    .toList();

            syncJobService.indexDataSaveAll(openApiCommands, worker, uuid);
        }
    }

    private int resolveFetchBatchSize() {
        int configuredSize = externalApiProperties.fetchBatchSize();
        if (configuredSize <= 0) {
            return MAX_FETCH_BATCH_SIZE;
        }

        return Math.min(configuredSize, MAX_FETCH_BATCH_SIZE);
    }

    private List<FetchOutcome> fetchBatch(List<StockIndexFetchTarget> batch) {
        List<CompletableFuture<FetchOutcome>> futures = batch.stream()
                .map(this::fetchAsync)
                .toList();

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .join();

        return futures.stream()
                .map(CompletableFuture::join)
                .toList();

    }

    /**
     * 단일 지수의 전체 페이지를 비동기로 조회합니다.
     */
    private CompletableFuture<FetchOutcome> fetchAsync(StockIndexFetchTarget target) {
        try {
            return CompletableFuture.
                    supplyAsync(
                            () -> fetchTarget(target.request()),
                            externalApiExecutor
                    )
                    .handle((items, throwable) -> {
                        if (throwable == null) {
                            return FetchOutcome.success(
                                    target,
                                    items
                            );
                        }

                        Throwable cause =
                                unwrap(throwable);

                        return FetchOutcome.failure(
                                target,
                                cause
                        );
                    });
        } catch (RejectedExecutionException e) {
            /*
             * Executor의 스레드와 큐가 모두 가득 차서
             * 작업 제출 자체가 거절된 경우입니다.
             */
            return CompletableFuture.completedFuture(
                    FetchOutcome.failure(
                            target,
                            e
                    )
            );
        }
    }

    private List<StockMarketIndex> fetchTarget(StockMarketIndexApiRequest request) {
        DataGoKrApiResponse<StockMarketIndex> firstResponse = dataGoKrApiService.getStockMarketIndexList(request);
        return fetchStockMarketIndexes(
                firstResponse,
                request
        );
    }

    private Throwable unwrap(
            Throwable throwable
    ) {
        Throwable current = throwable;

        while (current instanceof CompletionException
                && current.getCause() != null) {
            current = current.getCause();
        }

        return current;
    }

    /**
     * API 결과를 해당 IndexInfo의 저장 Command로 변환합니다.
     */
    private List<IndexDataOpenApiCommand> createIndexDataCommands(
            StockIndexFetchTarget target,
            List<StockMarketIndex> items
    ) {
        return items.stream()
                .filter(Objects::nonNull)
                .filter(item -> isSameIndexInfoKey(target.key, item))
                .map(item -> item.toIndexDataCommand(target.indexInfo))
                .toList();
    }

    private boolean isSameIndexInfoKey(IndexInfoKey key, StockMarketIndex item) {

        return Objects.equals(key.indexClassification(), item.idxCsf())
                && Objects.equals(key.indexName(), item.idxNm());
    }


    /**
     * 하나의 외부 API 호출 대상입니다.
     */
    private record StockIndexFetchTarget(
            IndexInfo indexInfo,
            IndexInfoKey key,
            StockMarketIndexApiRequest request
    ) {
    }

    /**
     * 외부 API 조회의 성공 또는 실패 결과입니다.
     */
    private record FetchOutcome(
            StockIndexFetchTarget target,
            List<StockMarketIndex> items,
            Throwable error
    ) {

        private FetchOutcome {
            items = items == null
                    ? List.of()
                    : List.copyOf(items);
        }

        static FetchOutcome success(
                StockIndexFetchTarget target,
                List<StockMarketIndex> items
        ) {
            return new FetchOutcome(
                    target,
                    items,
                    null
            );
        }

        static FetchOutcome failure(
                StockIndexFetchTarget target,
                Throwable error
        ) {
            return new FetchOutcome(
                    target,
                    List.of(),
                    error
            );
        }

        boolean isSuccess() {
            return error == null;
        }
    }

    private record PageFetchOutcome(
            int pageNo,
            List<StockMarketIndex> items,
            Throwable error
    ) {
        private PageFetchOutcome {
            items = items == null
                    ? List.of()
                    : List.copyOf(items);
        }

        static PageFetchOutcome success(
                int pageNo,
                List<StockMarketIndex> items
        ) {
            return new PageFetchOutcome(
                    pageNo,
                    items,
                    null
            );
        }

        static PageFetchOutcome failure(
                int pageNo,
                Throwable error
        ) {
            return new PageFetchOutcome(
                    pageNo,
                    List.of(),
                    error
            );
        }

        boolean isSuccess() {
            return error == null;
        }
    }


}
