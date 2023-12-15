package ru.itmo.zavar.highload.zorthprocessor.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthprocessor.config.feign.FeignConfiguration;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.response.GetCompilerOutResponse;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.response.CompileResponse;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.CompileRequest;

@Component
@ReactiveFeignClient(name = "zorth-translator-service", path = "${spring.webflux.base-path}", configuration = FeignConfiguration.class)
public interface ZorthTranslatorClient {
    @PostMapping("/compile")
    @CircuitBreaker(name = "ZorthTranslatorClientCB")
    Mono<CompileResponse> compile(@RequestBody CompileRequest compileRequest,
                                  @RequestHeader("username") String username,
                                  @RequestHeader("authorities") String authorities);

    @GetMapping(value = "/compiler-outs", params = "request-id")
    @CircuitBreaker(name = "ZorthTranslatorClientCB")
    Mono<GetCompilerOutResponse> getCompilerOutOfRequest(@RequestParam("request-id") Long requestId,
                                                         @RequestHeader("username") String username,
                                                         @RequestHeader("authorities") String authorities);
}
