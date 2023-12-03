package ru.itmo.zavar.highload.zorthtranslator.service;

import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.DebugMessagesEntity;

public interface DebugMessagesService {
    Mono<DebugMessagesEntity> save(DebugMessagesEntity debugMessagesEntity);

    Mono<DebugMessagesEntity> findById(Long id);

    Mono<DebugMessagesEntity> findByRequestId(Long requestId);

    Mono<Page<DebugMessagesEntity>> findAllPageable(Integer offset, Integer limit);
}