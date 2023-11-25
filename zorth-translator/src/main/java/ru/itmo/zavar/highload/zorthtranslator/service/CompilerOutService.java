package ru.itmo.zavar.highload.zorthtranslator.service;

import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.CompilerOutEntity;

public interface CompilerOutService {
    Mono<CompilerOutEntity> save(CompilerOutEntity compilerOutEntity);

    Mono<CompilerOutEntity> findById(Long id);

    Mono<CompilerOutEntity> findByRequestId(Long requestId);

    Mono<Page<CompilerOutEntity>> findAllPageable(Integer offset, Integer limit);
}
