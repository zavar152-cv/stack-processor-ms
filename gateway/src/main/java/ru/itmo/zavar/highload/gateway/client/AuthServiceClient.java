package ru.itmo.zavar.highload.gateway.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.gateway.config.feign.FeignConfiguration;
import ru.itmo.zavar.highload.gateway.dto.inner.request.ValidateTokenRequest;
import ru.itmo.zavar.highload.gateway.dto.inner.response.ValidateTokenResponse;

@Component
@ReactiveFeignClient(name = "auth", path = "${context-path}", configuration = FeignConfiguration.class)
public interface AuthServiceClient {
    @PostMapping("/token/validate")
    @CircuitBreaker(name = "AuthServiceClientCB")
    Mono<ValidateTokenResponse> validateToken(@RequestBody ValidateTokenRequest request);
}
