package com.sb13.findex.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ExternalApiExecutorConfig {

    @Bean
    public Executor externalApiExecutor(
            ExternalApiProperties properties
    ){

        ExternalApiProperties.ExecutorProperties executorProperties = properties.executor();
        validate(executorProperties);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(executorProperties.corePoolSize());
        executor.setMaxPoolSize(executorProperties.maxPoolSize());
        executor.setQueueCapacity(executorProperties.queueCapacity());
        executor.setThreadNamePrefix("external-api-");

        /*
         * 스레드 풀과 큐가 모두 가득 차면
         * 작업을 조용히 버리지 않고 예외를 발생시킵니다.
         */
        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.AbortPolicy()
        );

        /*
         * 애플리케이션 종료 시 이미 제출된 작업의 완료를 기다립니다.
         */
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(executorProperties.awaitTerminationSeconds());

        return executor;
    }

    private void validate(ExternalApiProperties.ExecutorProperties properties) {
        if(properties.maxPoolSize() < properties.corePoolSize()){
            throw new IllegalArgumentException( "maxPoolSize는 corePoolSize 이상이어야 합니다.");
        }
    }
}
