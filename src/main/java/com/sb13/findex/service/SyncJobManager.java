package com.sb13.findex.service;

import com.sb13.findex.domain.syncjob.JobType;
import com.sb13.findex.dto.command.IndexInfoKey;
import com.sb13.findex.dto.request.StockMarketIndexApiRequest;
import com.sb13.findex.dto.response.DataGoKrApiResponse;
import com.sb13.findex.dto.response.StockMarketIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SyncJobManager {

    private static final String SUCCESS_CODE = "00";

    private final DataGoKrApiService dataGoKrApiService;

    private final IpAddressService ipAddressService;

    public void syncIndexInfos() {
        DataGoKrApiResponse<StockMarketIndex> marketIndexResponse = dataGoKrApiService.getStockMarketIndexList();

        List<StockMarketIndex> stockMarketIndexList = getList(marketIndexResponse);

        Map<IndexInfoKey, StockMarketIndex> latestStockMarketIndices = getLatestStockMarketIndices(stockMarketIndexList);
        // TODO indexInfoService.saveAll

        JobType jobType = JobType.INDEX_INFO;
        String worker = ipAddressService.getClientIp();
        List<IndexInfoKey> indexInfoKeys = latestStockMarketIndices.keySet().stream().toList();
        // TODO syncJobService.saveAll

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
                        (exist, incoming) ->
                                exist.parseBasDt().isAfter(incoming.parseBasDt()) ?
                                        exist : incoming
                ));
    }

    private List<StockMarketIndex> fetchStockMarketIndexes(DataGoKrApiResponse<StockMarketIndex> marketIndexResponse) {
        Integer totalPages = marketIndexResponse.getTotalPages();
        Integer pageNo = marketIndexResponse.getPageNo();
        Integer numOfRows = marketIndexResponse.getNumOfRows();
        List<List<StockMarketIndex>> lists = new ArrayList<>();
        // 25만건 전부 호출시 1분 소요..
        for (int i = pageNo; i <= totalPages; i++) {
            DataGoKrApiResponse<StockMarketIndex> response = dataGoKrApiService.getStockMarketIndexList(StockMarketIndexApiRequest.ofPage(numOfRows, pageNo));
            lists.add(response.getItem());
        }
        lists.add(marketIndexResponse.getItem());
        return lists.stream().flatMap(List::stream).collect(Collectors.toList());
    }

}
