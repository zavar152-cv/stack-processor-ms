package ru.itmo.zavar.highload.zorthprocessor.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthprocessor.entity.zorth.ProcessorOutEntity;

public interface ProcessorOutService {
    Mono<ProcessorOutEntity> save(ProcessorOutEntity processorOutEntity);

    Flux<ProcessorOutEntity> findAllByCompilerOutId(Long compilerOutId);
}
