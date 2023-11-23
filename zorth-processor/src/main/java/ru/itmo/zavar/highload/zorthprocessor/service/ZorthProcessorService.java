package ru.itmo.zavar.highload.zorthprocessor.service;

import org.springframework.security.core.Authentication;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthprocessor.entity.zorth.ProcessorOutEntity;

public interface ZorthProcessorService {
    Mono<ProcessorOutEntity> pipeline(boolean debug, String text, String[] input, Authentication authentication);

    Mono<ProcessorOutEntity> startProcessorAndGetLogs(String[] input, Long requestId, Authentication authentication);

    Flux<ProcessorOutEntity> findAllProcessorOutByRequestId(Long requestId, Authentication authentication);
}
