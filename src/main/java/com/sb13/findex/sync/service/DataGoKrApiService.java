package com.sb13.findex.sync.service;

import com.sb13.findex.global.config.FindexApiProperties;
import com.sb13.findex.sync.dto.request.StockMarketIndexApiRequest;
import com.sb13.findex.sync.dto.response.DataGoKrApiResponse;
import com.sb13.findex.sync.dto.response.StockMarketIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataGoKrApiService {

    private final FindexApiProperties properties;
    private final RestClient findexRestClient;

    public DataGoKrApiResponse<StockMarketIndex> getStockMarketIndexList() {
        return getStockMarketIndexList(null);
    }

    public DataGoKrApiResponse<StockMarketIndex> getStockMarketIndexList(StockMarketIndexApiRequest request) {
       return call(
                properties.stockMarketEndpoint(),
                request,
                new ParameterizedTypeReference<DataGoKrApiResponse<StockMarketIndex>>() {
                }
        );
    }

    private <T> DataGoKrApiResponse<T> call(
            String endpoint,
            StockMarketIndexApiRequest request,
            ParameterizedTypeReference<DataGoKrApiResponse<T>> responseType
    ) {
        return findexRestClient.get().uri(
                        uriBuilder -> {
                            UriBuilder builder = uriBuilder
                                    .path("/" + endpoint)
                                    .queryParam("serviceKey", "{serviceKey}")
                                    .queryParam("resultType", "json");

                            appendQueryParams(request, builder);

                            return builder.build(properties.serviceKey());
                        }
                )
                .retrieve()
                .body(responseType);
    }

    private void appendQueryParams(StockMarketIndexApiRequest request, UriBuilder builder) {
        if (request == null) {
            // 페이지 건수 기본 설정 않을 시 사용합니다.
            /*
            builder.queryParam(
                    "numOfRows",10_000
            );
            */
            return;
        }

        request.toQueryParams()
                .keySet()
                .forEach(key ->
                        builder.queryParam(
                                key,
                                request.toQueryParams().get(key)
                        )
                );
    }


}
