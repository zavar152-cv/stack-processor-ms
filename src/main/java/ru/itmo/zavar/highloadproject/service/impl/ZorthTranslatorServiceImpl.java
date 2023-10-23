package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.InstructionCode;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ZorthTranslatorServiceImpl implements ZorthTranslatorService {

    private final ZorthTranslator translator = new ZorthTranslator(null, null, true);;

    private final CompilerOutRepository compilerOutRepository;
    private final DebugMessagesRepository debugMessagesRepository;
    private final RequestRepository requestRepository;
    private final ProcessorOutRepository processorOutRepository;

    @Override
    @Transactional
    public void compileAndLinkage(boolean debug, String text) {
        translator.compileFromString(debug, text);
        translator.linkage(debug);
        ProgramAndDataDto out = translator.getCompiledProgramAndDataInBytes();
        RequestEntity request = RequestEntity.builder().debug(debug).text(text).build();
        requestRepository.save(request);
        DebugMessagesEntity debugMessagesEntity = DebugMessagesEntity.builder()
                .request(request)
                .text(translator.getDebugMessages())
                .build();
        debugMessagesRepository.save(debugMessagesEntity);
        ArrayList<Long> data = new ArrayList<>();
        out.data().forEach(bInst -> data.add(InstructionCode.bytesToLong(ArrayUtils.toPrimitive(bInst))));
        ArrayList<Long> program = new ArrayList<>();
        out.program().forEach(bInst -> program.add(InstructionCode.bytesToLong(ArrayUtils.toPrimitive(bInst))));
        CompilerOutEntity compilerOutEntity = CompilerOutEntity.builder()
                .request(request)
                .data(data)
                .program(program)
                .build();
        compilerOutRepository.save(compilerOutEntity);
    }

    @Override
    public ProgramAndDataDto getCompilerOutput() {
        return translator.getCompiledProgramAndDataInBytes();
    }

    @Override
    public List<String> getDebugMessages() {
        return translator.getDebugMessages();
    }
}
