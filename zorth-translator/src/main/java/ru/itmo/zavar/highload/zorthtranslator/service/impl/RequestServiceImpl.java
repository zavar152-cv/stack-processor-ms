package ru.itmo.zavar.highload.zorthtranslator.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highload.zorthtranslator.repo.RequestRepository;
import ru.itmo.zavar.highload.zorthtranslator.service.RequestService;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;

    @Override
    public Mono<RequestEntity> save(RequestEntity requestEntity) {
        return Mono.fromCallable(() -> requestRepository.save(requestEntity))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<RequestEntity> findById(Long id) {
        return Mono.fromCallable(() -> requestRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Request not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> delete(RequestEntity requestEntity) {
        return Mono.fromRunnable(() -> requestRepository.delete(requestEntity))
                .subscribeOn(Schedulers.boundedElastic()).then();
    }
}
