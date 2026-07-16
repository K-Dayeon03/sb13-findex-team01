package com.sb13.findex.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "findex.external-api")
public record ExternalApiProperties(
        int fetchBatchSize,
        ExecutorProperties executor
) {
    record ExecutorProperties(
            int corePoolSize,
            int maxPoolSize,
            int queueCapacity,
            int awaitTerminationSeconds
    ){

    }
}
