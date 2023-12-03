package ru.itmo.zavar.highload.zorthtranslator.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highload.zorthtranslator.repo.CompilerOutRepository;
import ru.itmo.zavar.highload.zorthtranslator.service.CompilerOutService;
import ru.itmo.zavar.highload.zorthtranslator.service.RequestService;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CompilerOutServiceImpl implements CompilerOutService {
    private final CompilerOutRepository compilerOutRepository;
    private final RequestService requestService;

    @Override
    public Mono<CompilerOutEntity> save(CompilerOutEntity compilerOutEntity) {
        return Mono.fromCallable(() -> compilerOutRepository.save(compilerOutEntity))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<CompilerOutEntity> findById(Long id) {
        return Mono.fromCallable(() -> compilerOutRepository.findById(id)
                        .orElseThrow(() -> new NoSuchElementException("Compiler output not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<CompilerOutEntity> findByRequestId(Long requestId) {
        return requestService.findById(requestId)
                .map(requestEntity -> compilerOutRepository.findByRequest(requestEntity)
                        .orElseThrow(() -> new NoSuchElementException("Compiler output not found")))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Page<CompilerOutEntity>> findAllPageable(Integer offset, Integer limit) {
        return Mono.fromCallable(() -> compilerOutRepository.findAll(PageRequest.of(offset, limit)))
                .subscribeOn(Schedulers.boundedElastic());
    }
}