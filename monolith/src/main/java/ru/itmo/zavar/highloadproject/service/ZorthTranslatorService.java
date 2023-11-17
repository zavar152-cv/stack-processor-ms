package ru.itmo.zavar.highloadproject.service;

import org.springframework.data.domain.Page;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface ZorthTranslatorService {
    RequestEntity compileAndLinkage(boolean debug, String text, UserEntity userEntity) throws NoSuchElementException, ZorthException;

    Optional<CompilerOutEntity> getCompilerOutput(Long id);

    Page<CompilerOutEntity> getAllCompilerOutput(Integer offset, Integer limit);

    Optional<CompilerOutEntity> getCompilerOutputByRequestId(Long id);

    boolean checkRequestOwnedByUser(UserEntity userEntity, Long requestId);

    Optional<DebugMessagesEntity> getDebugMessages(Long id);

    Page<DebugMessagesEntity> getAllDebugMessages(Integer offset, Integer limit);

    Optional<DebugMessagesEntity> getDebugMessagesByRequestId(Long id);

    List<RequestEntity> getAllRequestsByUserId(Long id) throws IllegalArgumentException;
}
