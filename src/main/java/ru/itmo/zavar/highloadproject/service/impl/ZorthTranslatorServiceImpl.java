package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadproject.service.ZorthTranslatorService;
import ru.itmo.zavar.zorth.ProgramAndDataDto;
import ru.itmo.zavar.zorth.ZorthTranslator;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZorthTranslatorServiceImpl implements ZorthTranslatorService {

    private final ZorthTranslator translator = new ZorthTranslator(null, null, true);;

    @Override
    public void compileAndLinkage(boolean debug, String text) {
        translator.compileFromString(debug, text);
        translator.linkage(debug);
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
