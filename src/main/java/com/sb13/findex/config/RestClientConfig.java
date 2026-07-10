package com.sb13.findex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient findexRestClient(
            RestClient.Builder builder,
            FindexApiProperties properties
    ) {
        return builder
                .baseUrl(properties.baseUrl())
                .build();
    }

}
