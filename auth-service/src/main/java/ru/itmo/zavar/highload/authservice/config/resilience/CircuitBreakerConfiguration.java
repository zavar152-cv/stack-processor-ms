package ru.itmo.zavar.highload.authservice.config.resilience;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class CircuitBreakerConfiguration {
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .ignoreExceptions(FeignException.NotFound.class)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(5)
                .failureRateThreshold(40)
                .waitDurationInOpenState(Duration.ofMillis(10000))
                .enableAutomaticTransitionFromOpenToHalfOpen()
                .build();
    }

    @Bean
    public CircuitBreaker userServiceClientCircuitBreaker(CircuitBreakerConfig circuitBreakerConfig) {
        return circuitBreakerRegistry.circuitBreaker("UserServiceClientCB", circuitBreakerConfig);
    }
}
