package ru.itmo.zavar.highloadproject.service.impl;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.comp.ControlUnit;
import ru.itmo.zavar.exception.ControlUnitException;
import ru.itmo.zavar.highloadproject.clients.RequestServiceClient;
import ru.itmo.zavar.highloadproject.dto.inner.response.RequestServiceResponse;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.ProcessorOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.mapper.RequestEntityMapper;
import ru.itmo.zavar.highloadproject.service.*;
import ru.itmo.zavar.highloadproject.util.ZorthUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ZorthProcessorServiceImpl implements ZorthProcessorService {
    private final ProcessorOutService processorOutService;
    private final CompilerOutService compilerOutService;
    private final RequestServiceClient requestServiceClient;
    private final RequestEntityMapper mapper;

    @Override
    public ProcessorOutEntity startProcessorAndGetLogs(String[] input, CompilerOutEntity compilerOutEntity) throws ControlUnitException, ParseException {
        Gson gson = new Gson();
        String json = gson.toJson(input);
        JSONParser jsonParser = new JSONParser();
        JSONArray inputJson = (JSONArray) jsonParser.parse(json);

        ArrayList<Long> program = new ArrayList<>();
        ArrayList<Long> data = new ArrayList<>();

        byte[] bytesProg = compilerOutEntity.getProgram();
        List<Byte[]> instructions = ZorthUtil.splitArray(ArrayUtils.toObject(bytesProg));
        instructions.forEach(bInst -> program.add(InstructionCode.bytesToLong(ArrayUtils.toPrimitive(bInst))));

        byte[] bytesData = compilerOutEntity.getData();
        List<Byte[]> datas = ZorthUtil.splitArray(ArrayUtils.toObject(bytesData));
        datas.forEach(bData -> data.add(InstructionCode.bytesToLong(ArrayUtils.toPrimitive(bData))));

        ControlUnit controlUnit = new ControlUnit(program, data, inputJson, true);
        controlUnit.start();

        StringBuilder stringBuilder = new StringBuilder();
        controlUnit.getTickLog().forEach(tickLog -> {
            stringBuilder.append("\n");
            stringBuilder.append(tickLog.toString());
        });
        ProcessorOutEntity processorOutEntity = ProcessorOutEntity.builder()
                .tickLogs(stringBuilder.toString())
                .compilerOut(compilerOutEntity)
                .input(inputJson.toString())
                .build();
        return processorOutService.saveProcessorOut(processorOutEntity);
    }

    @Override
    public List<ProcessorOutEntity> getAllProcessorOutByRequest(Long requestId) throws NoSuchElementException {
        ResponseEntity<RequestServiceResponse> response = requestServiceClient.get(requestId);
        if (response.getStatusCode().isError()) {
            throw new NoSuchElementException();
        }
        RequestEntity requestEntity = mapper.fromResponse(response.getBody());
        CompilerOutEntity compilerOutEntity = compilerOutService.findByRequest(requestEntity).orElseThrow();
        return processorOutService.findAllByCompilerOut(compilerOutEntity);
    }
}
