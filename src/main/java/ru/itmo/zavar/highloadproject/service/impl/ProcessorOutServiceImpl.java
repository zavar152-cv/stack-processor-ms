package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.ProcessorOutEntity;
import ru.itmo.zavar.highloadproject.repo.ProcessorOutRepository;
import ru.itmo.zavar.highloadproject.service.ProcessorOutService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessorOutServiceImpl implements ProcessorOutService {
    private final ProcessorOutRepository processorOutRepository;
    @Override
    public ProcessorOutEntity saveProcessorOut(ProcessorOutEntity processorOutEntity) {
        return processorOutRepository.save(processorOutEntity);
    }

    @Override
    public List<ProcessorOutEntity> findAllByCompilerOut(CompilerOutEntity compilerOutEntity) {
        return processorOutRepository.findAllByCompilerOut(compilerOutEntity);
    }
}
