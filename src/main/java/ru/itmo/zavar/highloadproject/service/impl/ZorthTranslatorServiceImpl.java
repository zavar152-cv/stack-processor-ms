package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highloadproject.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.repo.*;
import ru.itmo.zavar.highloadproject.service.ZorthTranslatorService;
import ru.itmo.zavar.zorth.ProgramAndDataDto;
import ru.itmo.zavar.zorth.ZorthTranslator;

import javax.management.relation.Role;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ZorthTranslatorServiceImpl implements ZorthTranslatorService {
    private final CompilerOutRepository compilerOutRepository;
    private final DebugMessagesRepository debugMessagesRepository;
    private final RequestRepository requestRepository;
    private final ProcessorOutRepository processorOutRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void compileAndLinkage(boolean debug, String text, UserEntity userEntity) throws NoSuchElementException, IllegalArgumentException, ZorthException {
        ZorthTranslator translator = new ZorthTranslator(null, null, true);
        translator.compileFromString(debug, text);
        translator.linkage(debug);
        ProgramAndDataDto out = translator.getCompiledProgramAndDataInBytes();
        RequestEntity request = RequestEntity.builder().debug(debug).text(text).build();
        RoleEntity roleUser = roleRepository.findByName("ROLE_USER").orElseThrow();
        requestRepository.save(request);
        if (userEntity.getRoles().size() == 1 & userEntity.getRoles().contains(roleUser) & userEntity.getRequests().size() == 1) {
            RequestEntity requestEntity = userEntity.getRequests().get(0);
            compilerOutRepository.deleteByRequest(requestEntity);
            debugMessagesRepository.deleteByRequest(requestEntity);
            requestRepository.delete(requestEntity);
            userEntity.getRequests().clear();
        }
        userEntity.getRequests().add(request);
        userRepository.save(userEntity);
        if (debug) {
            DebugMessagesEntity debugMessagesEntity = DebugMessagesEntity.builder()
                    .request(request)
                    .text(String.join("\n", translator.getDebugMessages()))
                    .build();
            debugMessagesRepository.save(debugMessagesEntity);
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
        compilerOutRepository.save(compilerOutEntity);
    }

    @Override
    public Optional<CompilerOutEntity> getCompilerOutput(Long id) {
        return compilerOutRepository.findById(id);
    }

    @Override
    public Page<CompilerOutEntity> getAllCompilerOutput(Integer offset, Integer limit) {
        return compilerOutRepository.findAll(PageRequest.of(offset, limit));
    }

    @Override
    public Optional<CompilerOutEntity> getCompilerOutputByRequestId(Long id) {
        RequestEntity requestEntity = requestRepository.findById(id).orElseThrow();
        return compilerOutRepository.findByRequest(requestEntity);
    }

    @Override
    public boolean checkRequestOwnedByUser(UserEntity userEntity, Long requestId) {
        RequestEntity requestEntity = requestRepository.findById(requestId).orElseThrow();
        return userEntity.getRequests().contains(requestEntity);
    }

    @Override
    public Optional<DebugMessagesEntity> getDebugMessages(Long id) {
        return debugMessagesRepository.findById(id);
    }

    @Override
    public Page<DebugMessagesEntity> getAllDebugMessages(Integer offset, Integer limit) {
        return debugMessagesRepository.findAll(PageRequest.of(offset, limit));
    }

    @Override
    public Optional<DebugMessagesEntity> getDebugMessagesByRequestId(Long id) {
        RequestEntity requestEntity = requestRepository.findById(id).orElseThrow();
        return debugMessagesRepository.findByRequest(requestEntity);
    }

    @Override
    public List<RequestEntity> getAllRequestsByUserId(Long id) throws IllegalArgumentException {
        Optional<UserEntity> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        return optionalUser.get().getRequests();
    }
}
