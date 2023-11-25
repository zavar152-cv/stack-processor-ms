package ru.itmo.zavar.highload.zorthtranslator.service;

import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

public interface RequestService {
    Mono<RequestEntity> save(RequestEntity requestEntity);

    Mono<RequestEntity> findById(Long id);

    Mono<Void> delete(RequestEntity requestEntity);
}
