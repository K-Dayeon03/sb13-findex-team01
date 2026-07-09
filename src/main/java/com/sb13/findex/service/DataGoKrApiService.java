package com.sb13.findex.service;

import com.sb13.findex.config.FindexApiProperties;
import com.sb13.findex.dto.request.StockMarketIndexApiRequest;
import com.sb13.findex.dto.response.DataGoKrApiResponse;
import com.sb13.findex.dto.response.StockMarketIndex;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataGoKrApiService {

    private final FindexApiProperties properties;
    private final RestClient findexRestClient;

    public List<StockMarketIndex> getStockMarketIndexList() {
        log.info("serviceKey = {}", properties.serviceKey());
        log.info("baseUrl = {}", properties.baseUrl());
        log.info("endpoint = {}", properties.stockMarketEndpoint());

        DataGoKrApiResponse<StockMarketIndex> stockMarketIndexResponse = call(
                properties.stockMarketEndpoint(),
                new ParameterizedTypeReference<DataGoKrApiResponse<StockMarketIndex>>() {
                }
        );

        return getList(stockMarketIndexResponse);
    }

    private <T> List<T> getList(DataGoKrApiResponse<T> response) {
        if (isResponseError(response)) {
            log.error("response : {}", response);
            return List.of();
        }
        log.info("response : {}", response);
        return response.getItem();
    }

    private static <T> boolean isResponseError(DataGoKrApiResponse<T> response) {
        return !"00".equals(response.getResultCode());
    }

    public List<StockMarketIndex> getStockMarketIndexList(StockMarketIndexApiRequest request) {
        DataGoKrApiResponse<StockMarketIndex> stockMarketIndexResponse = call(
                properties.stockMarketEndpoint(),
                request,
                new ParameterizedTypeReference<DataGoKrApiResponse<StockMarketIndex>>() {
                }
        );

        return getList(stockMarketIndexResponse);
    }

    private <T> DataGoKrApiResponse<T> call(
            String endpoint,
            ParameterizedTypeReference<DataGoKrApiResponse<T>> responseType
    ) {

        return findexRestClient.get()
                .uri(uriBuilder -> {
                    URI uri = uriBuilder
                            .path("/" + endpoint)
                            .queryParam("serviceKey", "{serviceKey}")
                            .queryParam("resultType", "json")
                            .build(properties.serviceKey());

                    log.info("request uri = {}", uri);
                    return uri;
                })
                .retrieve()
                .body(responseType);
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
        if (request == null) return;

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
