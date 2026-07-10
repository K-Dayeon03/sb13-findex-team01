package com.sb13.findex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "findex.api")
public record FindexApiProperties(
        String baseUrl,
        String serviceKey,
        String stockMarketEndpoint
) {

}
