package ru.itmo.zavar.highloadproject.service;

import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.ProcessorOutEntity;

import java.util.List;

public interface ProcessorOutService {
    ProcessorOutEntity saveProcessorOut(ProcessorOutEntity processorOutEntity);

    List<ProcessorOutEntity> findAllByCompilerOut(CompilerOutEntity compilerOutEntity);
}
