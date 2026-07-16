package com.sb13.findex.global.config;

import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient findexRestClient(
            RestClient.Builder builder,
            FindexApiProperties properties
    ) {

        return builder
                .baseUrl(properties.baseUrl())
                .requestFactory(createClientHttpRequestFactory(initializeHttpRequestSettings(properties)))
                .build();
    }

    private ClientHttpRequestFactorySettings initializeHttpRequestSettings(FindexApiProperties properties) {
        return ClientHttpRequestFactorySettings.defaults()
                .withTimeouts(
                        properties.connectTimeout(),
                        properties.readTimeout()
                );
    }

    private static ClientHttpRequestFactory createClientHttpRequestFactory(ClientHttpRequestFactorySettings settings) {
        return ClientHttpRequestFactoryBuilder
                .detect()
                .build(settings);
    }

}
