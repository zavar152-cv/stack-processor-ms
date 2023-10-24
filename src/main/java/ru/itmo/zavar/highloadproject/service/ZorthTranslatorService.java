package ru.itmo.zavar.highloadproject.service;

import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;
import ru.itmo.zavar.zorth.ProgramAndDataDto;

import java.util.List;
import java.util.Optional;

public interface ZorthTranslatorService {
    void compileAndLinkage(boolean debug, String text, UserEntity userEntity);

    Optional<CompilerOutEntity> getCompilerOutput(Long id);

    Optional<DebugMessagesEntity> getDebugMessages(Long id);

    List<RequestEntity> getAllRequestsByUserId(Long id) throws IllegalArgumentException;
}
