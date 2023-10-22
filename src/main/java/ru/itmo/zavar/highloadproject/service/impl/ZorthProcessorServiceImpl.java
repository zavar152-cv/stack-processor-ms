package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.comp.ControlUnit;
import ru.itmo.zavar.exception.ControlUnitException;
import ru.itmo.zavar.highloadproject.service.ZorthProcessorService;
import ru.itmo.zavar.log.TickLog;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ZorthProcessorServiceImpl implements ZorthProcessorService {
    @Override
    public List<TickLog> startProcessorAndGetLogs(ArrayList<Long> program, ArrayList<Long> data, JSONArray input, boolean debug) throws ControlUnitException {
        ControlUnit controlUnit = new ControlUnit(program, data, input, debug);
        controlUnit.start();
        return controlUnit.getTickLog();
    }
}
