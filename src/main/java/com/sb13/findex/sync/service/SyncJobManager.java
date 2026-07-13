package com.sb13.findex.sync.service;


import com.sb13.findex.indexdata.service.IndexDataService;
import com.sb13.findex.indexinfo.entity.IndexInfo;
import com.sb13.findex.sync.dto.command.IndexDataSyncCommand;
import com.sb13.findex.sync.dto.command.IndexInfoKey;
import com.sb13.findex.sync.dto.request.StockMarketIndexApiRequest;
import com.sb13.findex.sync.dto.response.DataGoKrApiResponse;
import com.sb13.findex.sync.dto.response.StockMarketIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncJobManager {

    private static final String SUCCESS_CODE = "00";

    private final DataGoKrApiService dataGoKrApiService;

    private final IpAddressService ipAddressService;

    private final Executor externalApiExecutor;

    private final IndexDataService indexDataService;

    public void syncIndexInfos() {
        DataGoKrApiResponse<StockMarketIndex> response = dataGoKrApiService.getStockMarketIndexList();

        // 단건 : 기본 설정으로 총 데이터 : 10
        List<StockMarketIndex> stockMarketIndexList = getList(response);

        // 복수 : 총 지수정보 데이터: 228건
        // 반복 호출 사용시 아래 로직을 사용예정.
        // List<StockMarketIndex> stockMarketIndexList = fetchStockMarketIndexes(response);
        if (stockMarketIndexList.isEmpty()) {
            log.warn("동기화할 주가지수 데이터가 없습니다.");
            return;
        }

        Map<IndexInfoKey, StockMarketIndex> latestStockMarketIndices = getLatestStockMarketIndices(stockMarketIndexList);
        log.info("latestStockMarketIndices.size : {}", latestStockMarketIndices.size());
        /*
         * TODO indexInfoService.saveAll
         *  - indexInfo의 저장받을 Dto가 필요합니다.
         *  - 메서드와 Dto가 정의된 이후 작업예정입니다.
         */


        String worker = ipAddressService.getClientIp();
        List<IndexInfoKey> indexInfoKeys = latestStockMarketIndices.keySet().stream().toList();
        /*
         * TODO syncJobService.saveAll
         *  - syncJobService 가 공유되면 작업 예정입니다.
         */

    }

    public void syncIndexDataList(IndexDataSyncCommand command, String worker){
        List<Integer> indexInfoIds = command.indexInfoIds();
        LocalDate baseDateFrom = command.baseDateFrom();
        LocalDate baseDateTo = command.baseDateTo();

        /*
         * TODO
         *  1. indexInfo의 목록 EX) indexInfoService.getIndexInfoByIds
         *  - indexInfoService 가 공유되면 작업 예정입니다.
         */
        List<IndexInfo> indexInfos = new ArrayList<>();

        /*
         * TODO
         *  2. indexInfo의 키 분류..
         */
        Map<IndexInfoKey,IndexInfo> infoKeyIndexInfoMap = indexInfos.stream()
              .collect(Collectors.toMap(this::createIndexInfoKey, Function.identity()));

        /*
         * TODO
         *  3. indexData를 만들기 위한 StockMarketIndexApiRequest 생성
         */
        List<StockMarketIndexApiRequest> apiRequests = infoKeyIndexInfoMap.keySet().stream()
              .map(key -> StockMarketIndexApiRequest.ofExactIndexName(baseDateFrom, baseDateTo, key.indexName()))
              .toList();

        /*
         * TODO
         *  4. API 호출
         */
        List<StockMarketIndex> filteredIndexes = apiRequests.stream()
              .map(request -> fetchStockMarketIndexes(dataGoKrApiService.getStockMarketIndexList(request)))
              .flatMap(List::stream)
              .filter(smi -> infoKeyIndexInfoMap.get(IndexInfoKey.from(smi)) != null)
              .toList();

        /*
         * TODO
         *  5. IndexData 저장
         *  - indexDataService.saveOrUpdateOpenApiData  가 공유되면 작업 예정입니다.
         */
        filteredIndexes.forEach(smi -> {
            indexDataService.saveOrUpdateOpenApiData(smi.toIndexDataCommand(infoKeyIndexInfoMap.get(IndexInfoKey.from(smi))));
        });

        /*
         * TODO
         *  6. SyncJob 저장
         *  - syncJobService 가 공유되면 작업 예정입니다.
         */


    }

    private IndexInfoKey createIndexInfoKey(IndexInfo info) {
        return new IndexInfoKey(info.getIndexClassification(), info.getIndexName());
    }

    public void syncIndexDataList(IndexDataSyncCommand command){
        syncIndexDataList(command, ipAddressService.getClientIp());
    }

    private <T> List<T> getList(DataGoKrApiResponse<T> response) {
        if (isResponseError(response)) {
            log.error("response : {}", response);
            return List.of();
        }
        return response.getItem();
    }

    private <T> boolean isResponseError(DataGoKrApiResponse<T> response) {
        return !SUCCESS_CODE.equals(response.getResultCode());
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

    private List<StockMarketIndex> fetchStockMarketIndexes(DataGoKrApiResponse<StockMarketIndex> firstResponse) {
        Integer totalPages = firstResponse.getTotalPages();
        Integer pageNo = firstResponse.getPageNo();
        Integer numOfRows = firstResponse.getNumOfRows();
        if (totalPages == null || pageNo == null || numOfRows == null) {
            log.error(
                    "주가지수 API 페이지 정보가 올바르지 않습니다. " +
                            "pageNo={}, numOfRows={}, totalPages={}",
                    pageNo,
                    numOfRows,
                    totalPages
            );
            return List.of();
        }

        List<StockMarketIndex> lists = new ArrayList<>(firstResponse.getItem());
        // 25만건 전부 호출시 1분 소요..
        // 비동기 멀티쓰레드 사용 고려 예정
        for (int currentPage = pageNo + 1; currentPage <= totalPages; currentPage++) {
            DataGoKrApiResponse<StockMarketIndex> pageResponse = dataGoKrApiService.getStockMarketIndexList(StockMarketIndexApiRequest.ofPage(numOfRows, currentPage));

            if (isResponseError(pageResponse)) {
                log.error(
                        "주가지수 페이지 조회에 실패했습니다. page={}, response={}",
                        currentPage,
                        pageResponse
                );

                // 일부 페이지만 저장되는 것을 방지하기 위해 전체 동기화를 중단
                return List.of();
            }
            lists.addAll(pageResponse.getItem());
        }
        return lists;
    }

}
