package ru.itmo.zavar.highloadproject.service;

import org.json.simple.JSONArray;
import ru.itmo.zavar.exception.ControlUnitException;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.ProcessorOutEntity;
import ru.itmo.zavar.log.TickLog;

import java.util.ArrayList;
import java.util.List;

public interface ZorthProcessorService {
    ProcessorOutEntity startProcessorAndGetLogs(JSONArray input, CompilerOutEntity compilerOutEntity) throws ControlUnitException;

    List<ProcessorOutEntity> getAllProcessorOutByRequest(Long requestId);
}
