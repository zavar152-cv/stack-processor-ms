package ru.itmo.zavar.highload.zorthprocessor.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthprocessor.entity.zorth.ProcessorOutEntity;
import ru.itmo.zavar.highload.zorthprocessor.repo.ProcessorOutRepository;
import ru.itmo.zavar.highload.zorthprocessor.service.ProcessorOutService;

@Service
@RequiredArgsConstructor
public class ProcessorOutServiceImpl implements ProcessorOutService {
    private final ProcessorOutRepository processorOutRepository;

    @Override
    public Mono<ProcessorOutEntity> save(ProcessorOutEntity entity) {
        return processorOutRepository.saveWithLargeObjects(entity.getCompilerOutId(), entity.getInput(), entity.getTickLogs())
                .map(id -> {
                    entity.setId(id);
                    return entity;
                });
    }

    @Override
    public Flux<ProcessorOutEntity> findAllByCompilerOutId(Long compilerOutId) {
        return processorOutRepository.findAllByCompilerOutId(compilerOutId);
    }
}
