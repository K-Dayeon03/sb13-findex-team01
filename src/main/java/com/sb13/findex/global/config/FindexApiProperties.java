package com.sb13.findex.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "findex.api")
public record FindexApiProperties(
        String baseUrl,
        String serviceKey,
        String stockMarketEndpoint,
        Duration connectTimeout,
        Duration readTimeout
) {

}
