package ru.itmo.zavar.highloadproject.service;

import org.json.simple.JSONArray;
import ru.itmo.zavar.exception.ControlUnitException;
import ru.itmo.zavar.log.TickLog;

import java.util.ArrayList;
import java.util.List;

public interface ZorthProcessorService {
    List<TickLog> startProcessorAndGetLogs(ArrayList<Long> program, ArrayList<Long> data, JSONArray input, boolean debug) throws ControlUnitException;
}
