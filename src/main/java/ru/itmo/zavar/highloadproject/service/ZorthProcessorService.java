package ru.itmo.zavar.highloadproject.service;

import org.json.simple.parser.ParseException;
import ru.itmo.zavar.exception.ControlUnitException;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.ProcessorOutEntity;

import java.util.List;

public interface ZorthProcessorService {
    ProcessorOutEntity startProcessorAndGetLogs(String[] input, CompilerOutEntity compilerOutEntity) throws ControlUnitException, ParseException;

    List<ProcessorOutEntity> getAllProcessorOutByRequest(Long requestId);
}
