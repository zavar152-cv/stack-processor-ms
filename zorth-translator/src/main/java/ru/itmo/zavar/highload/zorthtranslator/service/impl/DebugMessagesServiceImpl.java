package ru.itmo.zavar.highload.zorthtranslator.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highload.zorthtranslator.repo.DebugMessagesRepository;
import ru.itmo.zavar.highload.zorthtranslator.service.DebugMessagesService;
import ru.itmo.zavar.highload.zorthtranslator.service.RequestService;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DebugMessagesServiceImpl implements DebugMessagesService {
    private final DebugMessagesRepository debugMessagesRepository;
    private final RequestService requestService;

    @Override
    public Mono<DebugMessagesEntity> save(DebugMessagesEntity debugMessagesEntity) {
        return Mono.fromCallable(() -> debugMessagesRepository.save(debugMessagesEntity))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<DebugMessagesEntity> findById(Long id) {
        return Mono.fromCallable(() -> debugMessagesRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Debug messages not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<DebugMessagesEntity> findByRequestId(Long requestId) {
        return requestService.findById(requestId)
                .map(requestEntity -> debugMessagesRepository.findByRequest(requestEntity)
                        .orElseThrow(() -> new NoSuchElementException("Debug messages not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Page<DebugMessagesEntity>> findAllPageable(Integer offset, Integer limit) {
        return Mono.fromCallable(() -> debugMessagesRepository.findAll(PageRequest.of(offset, limit)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
