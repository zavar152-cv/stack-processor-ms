package ru.itmo.zavar.highload.zorthprocessor.service;

import org.json.simple.parser.ParseException;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.exception.ControlUnitException;
import ru.itmo.zavar.highload.zorthprocessor.entity.zorth.ProcessorOutEntity;

public interface ZorthProcessorService {
    Mono<ProcessorOutEntity> startProcessorAndGetLogs(String[] input, Long requestId, Authentication authentication)
            throws ControlUnitException, ParseException, ResponseStatusException;

    Flux<ProcessorOutEntity> findAllProcessorOutByRequestId(Long requestId, Authentication authentication);
}
