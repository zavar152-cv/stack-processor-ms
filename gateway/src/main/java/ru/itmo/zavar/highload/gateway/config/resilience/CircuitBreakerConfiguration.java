package ru.itmo.zavar.highload.gateway.config.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class CircuitBreakerConfiguration {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .ignoreException(e -> e instanceof ResponseStatusException ex && ex.getStatusCode().is4xxClientError())
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .failureRateThreshold(40)
                .waitDurationInOpenState(Duration.ofMillis(10000))
                .permittedNumberOfCallsInHalfOpenState(2)
                .build();
    }

    @Bean
    public CircuitBreaker userServiceClientCircuitBreaker(CircuitBreakerConfig circuitBreakerConfig) {
        return circuitBreakerRegistry.circuitBreaker("AuthServiceClientCB", circuitBreakerConfig);
    }
}
