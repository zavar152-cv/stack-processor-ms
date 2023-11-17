package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highloadproject.clients.RequestServiceClient;
import ru.itmo.zavar.highloadproject.clients.UserServiceClient;
import ru.itmo.zavar.highloadproject.dto.inner.request.RequestServiceRequest;
import ru.itmo.zavar.highloadproject.dto.inner.response.RequestServiceResponse;
import ru.itmo.zavar.highloadproject.dto.inner.response.UserServiceResponse;
import ru.itmo.zavar.highloadproject.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.mapper.RequestEntityMapper;
import ru.itmo.zavar.highloadproject.mapper.UserEntityMapper;
import ru.itmo.zavar.highloadproject.service.*;
import ru.itmo.zavar.zorth.ProgramAndDataDto;
import ru.itmo.zavar.zorth.ZorthTranslator;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ZorthTranslatorServiceImpl implements ZorthTranslatorService {
    private final CompilerOutService compilerOutService;
    private final DebugMessagesService debugMessagesService;
    private final RequestServiceClient requestServiceClient;
    private final UserServiceClient userServiceClient;
    private final RoleService roleService;
    private final RequestEntityMapper requestEntityMapper;
    private final UserEntityMapper userEntityMapper;

    @Override
    @Transactional
    public RequestEntity compileAndLinkage(boolean debug, String text, UserEntity userEntity) throws NoSuchElementException, IllegalArgumentException, ZorthException {
        ZorthTranslator translator = new ZorthTranslator(null, null, true);
        translator.compileFromString(debug, text);
        translator.linkage(debug);
        ProgramAndDataDto out = translator.getCompiledProgramAndDataInBytes();
        RequestServiceRequest request = RequestServiceRequest.builder().debug(debug).text(text).build();
        RoleEntity roleUser = roleService.getUserRole().orElseThrow();
        ResponseEntity<RequestServiceResponse> responseAfterSave = requestServiceClient.save(request);
        if (responseAfterSave.getStatusCode().isError()) {
            throw new NoSuchElementException();
        }
        RequestEntity requestToReturn = requestEntityMapper.fromResponse(responseAfterSave.getBody());
        if (userEntity.getRoles().size() == 1 & userEntity.getRoles().contains(roleUser) & !userEntity.getRequests().isEmpty()) {
            RequestEntity requestEntity = userEntity.getRequests().get(0);
            compilerOutService.deleteCompilerOutByRequest(requestEntity);
            debugMessagesService.deleteDebugMessagesByRequest(requestEntity);
            ResponseEntity<?> responseAfterDelete = requestServiceClient.delete(request);
            if (responseAfterDelete.getStatusCode().isError()) {
                throw new NoSuchElementException();
            }
            userEntity.getRequests().clear();
        }
        userEntity.getRequests().add(requestToReturn);
        userServiceClient.save(userEntityMapper.toRequest(userEntity));
        if (debug) {
            DebugMessagesEntity debugMessagesEntity = DebugMessagesEntity.builder()
                    .request(requestToReturn)
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
                .request(requestToReturn)
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
    public Optional<CompilerOutEntity> getCompilerOutputByRequestId(Long id) throws NoSuchElementException {
        ResponseEntity<RequestServiceResponse> response = requestServiceClient.get(id);
        if (response.getStatusCode().isError()) {
            throw new NoSuchElementException();
        }
        RequestEntity requestEntity = requestEntityMapper.fromResponse(response.getBody());
        return compilerOutService.findByRequest(requestEntity);
    }

    @Override
    public boolean checkRequestOwnedByUser(UserEntity userEntity, Long requestId) throws NoSuchElementException {
        ResponseEntity<RequestServiceResponse> response = requestServiceClient.get(requestId);
        if (response.getStatusCode().isError()) {
            throw new NoSuchElementException();
        }
        RequestEntity requestEntity = requestEntityMapper.fromResponse(response.getBody());
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
    public Optional<DebugMessagesEntity> getDebugMessagesByRequestId(Long id) throws NoSuchElementException {
        ResponseEntity<RequestServiceResponse> response = requestServiceClient.get(id);
        if (response.getStatusCode().isError()) {
            throw new NoSuchElementException();
        }
        RequestEntity requestEntity = requestEntityMapper.fromResponse(response.getBody());
        return debugMessagesService.findByRequest(requestEntity);
    }

    @Override
    public List<RequestEntity> getAllRequestsByUserId(Long id) throws IllegalArgumentException {
        ResponseEntity<UserServiceResponse> response = userServiceClient.getById(id);
        if (response.getStatusCode().isError()) {
            throw new IllegalArgumentException("User not found");
        }
        return userEntityMapper.fromResponse(response.getBody()).getRequests();
    }
}
