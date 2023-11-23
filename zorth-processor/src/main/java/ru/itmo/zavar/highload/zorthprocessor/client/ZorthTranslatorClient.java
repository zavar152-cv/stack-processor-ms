package ru.itmo.zavar.highload.zorthprocessor.client;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthprocessor.config.feign.FeignConfiguration;
import ru.itmo.zavar.highload.zorthprocessor.dto.inner.CompilerOutDTO;
import ru.itmo.zavar.highload.zorthprocessor.dto.inner.RequestDTO;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.CompileRequest;

@Component
@ReactiveFeignClient(name = "zorth-translator", path = "${spring.webflux.base-path}", configuration = FeignConfiguration.class)
public interface ZorthTranslatorClient {
    @PostMapping("/compile")
    Mono<RequestDTO> compile(@RequestBody CompileRequest compileRequest,
                             @RequestHeader("username") String username,
                             @RequestHeader("authorities") String authorities);

    @GetMapping(value = "/compiler-outs", params = "request-id")
    Mono<CompilerOutDTO> getCompilerOutOfRequest(@RequestParam("request-id") Long requestId,
                                                 @RequestHeader("username") String username,
                                                 @RequestHeader("authorities") String authorities);
}
