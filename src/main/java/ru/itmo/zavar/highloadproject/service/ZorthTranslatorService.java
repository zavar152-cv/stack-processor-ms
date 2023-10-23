package ru.itmo.zavar.highloadproject.service;

import ru.itmo.zavar.zorth.ProgramAndDataDto;

import java.util.List;

public interface ZorthTranslatorService {
    void compileAndLinkage(boolean debug, String text);

    ProgramAndDataDto getCompilerOutput();

    List<String> getDebugMessages();
}
