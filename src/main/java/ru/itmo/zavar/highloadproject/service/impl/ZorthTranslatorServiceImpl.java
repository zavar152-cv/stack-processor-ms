package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.repo.CompilerOutRepository;
import ru.itmo.zavar.highloadproject.repo.DebugMessagesRepository;
import ru.itmo.zavar.highloadproject.repo.ProcessorOutRepository;
import ru.itmo.zavar.highloadproject.repo.RequestRepository;
import ru.itmo.zavar.highloadproject.service.ZorthTranslatorService;
import ru.itmo.zavar.zorth.ProgramAndDataDto;
import ru.itmo.zavar.zorth.ZorthTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ZorthTranslatorServiceImpl implements ZorthTranslatorService {

    private final ZorthTranslator translator = new ZorthTranslator(null, null, true);;

    private final CompilerOutRepository compilerOutRepository;
    private final DebugMessagesRepository debugMessagesRepository;
    private final RequestRepository requestRepository;
    private final ProcessorOutRepository processorOutRepository;

    @Override
    @Transactional //TODO change text to blob in entity
    public void compileAndLinkage(boolean debug, String text) {
        translator.compileFromString(debug, text);
        translator.linkage(debug);
        ProgramAndDataDto out = translator.getCompiledProgramAndDataInBytes();
        RequestEntity request = RequestEntity.builder().debug(debug).text(text).build();
        requestRepository.save(request);
        DebugMessagesEntity debugMessagesEntity = DebugMessagesEntity.builder()
                .request(request)
                .text(String.join("\n", translator.getDebugMessages()))
                .build();
        debugMessagesRepository.save(debugMessagesEntity);

        ArrayList<Byte> data = new ArrayList<>();
        out.data().forEach(bytes -> data.addAll(Arrays.stream(bytes).toList()));
        Byte[] dataArray = new Byte[data.size()];
        data.toArray(dataArray);

        ArrayList<Byte> program = new ArrayList<>();
        out.program().forEach(bytes -> program.addAll(Arrays.stream(bytes).toList()));
        Byte[] programArray = new Byte[program.size()];
        program.toArray(programArray);

        CompilerOutEntity compilerOutEntity = CompilerOutEntity.builder()
                .request(request)
                .data(ArrayUtils.toPrimitive(dataArray))
                .program(ArrayUtils.toPrimitive(programArray))
                .build();
        compilerOutRepository.save(compilerOutEntity);
    }

    @Override
    public Optional<CompilerOutEntity> getCompilerOutput(Long id) {
        return compilerOutRepository.findById(id);
    }

    @Override
    public Optional<DebugMessagesEntity> getDebugMessages(Long id) {
        return debugMessagesRepository.findById(id);
    }
}
