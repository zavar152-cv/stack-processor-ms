package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highloadproject.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.service.*;
import ru.itmo.zavar.zorth.ProgramAndDataDto;
import ru.itmo.zavar.zorth.ZorthTranslator;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ZorthTranslatorServiceImpl implements ZorthTranslatorService {
    private final CompilerOutService compilerOutService;
    private final DebugMessagesService debugMessagesService;
    private final RequestService requestService;
    private final UserService userService;
    private final RoleService roleService;

    @Override
    @Transactional
    public RequestEntity compileAndLinkage(boolean debug, String text, UserEntity userEntity) throws NoSuchElementException, IllegalArgumentException, ZorthException {
        ZorthTranslator translator = new ZorthTranslator(null, null, true);
        translator.compileFromString(debug, text);
        translator.linkage(debug);
        ProgramAndDataDto out = translator.getCompiledProgramAndDataInBytes();
        RequestEntity request = RequestEntity.builder().debug(debug).text(text).build();
        RoleEntity roleUser = roleService.getUserRole().orElseThrow();
        RequestEntity requestToReturn = requestService.saveRequest(request);
        if (userEntity.getRoles().size() == 1 & userEntity.getRoles().contains(roleUser) & !userEntity.getRequests().isEmpty()) {
            RequestEntity requestEntity = userEntity.getRequests().get(0);
            compilerOutService.deleteCompilerOutByRequest(requestEntity);
            debugMessagesService.deleteDebugMessagesByRequest(requestEntity);
            requestService.deleteRequest(requestEntity);
            userEntity.getRequests().clear();
        }
        userEntity.getRequests().add(request);
        userService.saveUser(userEntity);
        if (debug) {
            DebugMessagesEntity debugMessagesEntity = DebugMessagesEntity.builder()
                    .request(request)
                    .text(String.join("\n", translator.getDebugMessages()))
                    .build();
            debugMessagesService.saveDebugMessages(debugMessagesEntity);
        }

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
        compilerOutService.saveCompilerOut(compilerOutEntity);
        return requestToReturn;
    }

    @Override
    public Optional<CompilerOutEntity> getCompilerOutput(Long id) {
        return compilerOutService.findById(id);
    }

    @Override
    public Page<CompilerOutEntity> getAllCompilerOutput(Integer offset, Integer limit) {
        return compilerOutService.findAllPageable(offset, limit);
    }

    @Override
    public Optional<CompilerOutEntity> getCompilerOutputByRequestId(Long id) {
        RequestEntity requestEntity = requestService.findById(id).orElseThrow();
        return compilerOutService.findByRequest(requestEntity);
    }

    @Override
    public boolean checkRequestOwnedByUser(UserEntity userEntity, Long requestId) {
        RequestEntity requestEntity = requestService.findById(requestId).orElseThrow();
        return userEntity.getRequests().contains(requestEntity);
    }

    @Override
    public Optional<DebugMessagesEntity> getDebugMessages(Long id) {
        return debugMessagesService.findById(id);
    }

    @Override
    public Page<DebugMessagesEntity> getAllDebugMessages(Integer offset, Integer limit) {
        return debugMessagesService.findAllPageable(offset, limit);
    }

    @Override
    public Optional<DebugMessagesEntity> getDebugMessagesByRequestId(Long id) {
        RequestEntity requestEntity = requestService.findById(id).orElseThrow();
        return debugMessagesService.findByRequest(requestEntity);
    }

    @Override
    public List<RequestEntity> getAllRequestsByUserId(Long id) throws IllegalArgumentException {
        Optional<UserEntity> optionalUser = userService.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        return optionalUser.get().getRequests();
    }
}
