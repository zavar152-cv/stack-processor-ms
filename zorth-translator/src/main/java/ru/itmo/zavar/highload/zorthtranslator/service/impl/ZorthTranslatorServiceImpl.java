package ru.itmo.zavar.highload.zorthtranslator.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthtranslator.client.UserServiceClient;
import ru.itmo.zavar.highload.zorthtranslator.entity.security.RoleEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highload.zorthtranslator.mapper.RoleEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.mapper.UserEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.service.CompilerOutService;
import ru.itmo.zavar.highload.zorthtranslator.service.DebugMessagesService;
import ru.itmo.zavar.highload.zorthtranslator.service.RequestService;
import ru.itmo.zavar.highload.zorthtranslator.service.ZorthTranslatorService;
import ru.itmo.zavar.highload.zorthtranslator.util.RoleConstants;
import ru.itmo.zavar.zorth.ProgramAndDataDto;
import ru.itmo.zavar.zorth.ZorthTranslator;

import java.util.Arrays;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ZorthTranslatorServiceImpl implements ZorthTranslatorService {
    private final RequestService requestService;
    private final CompilerOutService compilerOutService;
    private final DebugMessagesService debugMessagesService;

    private final UserServiceClient userServiceClient;
    private final UserEntityMapper userEntityMapper;
    private final RoleEntityMapper roleEntityMapper;

    @Override
    public Mono<RequestEntity> compileAndLinkage(boolean debug, String text, String username) {
        /* Вызываем транслятор */
        ZorthTranslator translator = new ZorthTranslator(null, null, true);
        translator.compileFromString(debug, text);
        translator.linkage(debug);
        ProgramAndDataDto out = translator.getCompiledProgramAndDataInBytes();

        /* Сохраняем всё в бд */
        RequestEntity requestEntity = RequestEntity.builder().debug(debug).text(text).build();
        return requestService.save(requestEntity)
                .flatMap(savedRequestEntity -> saveDebugMessages(savedRequestEntity, debug, translator))
                .flatMap(savedRequestEntity -> saveCompilerOut(savedRequestEntity, out))
                .flatMap(savedRequestEntity -> addRequestToUser(savedRequestEntity, username));
    }

    private Mono<RequestEntity> saveDebugMessages(RequestEntity requestEntity, boolean debug, ZorthTranslator translator) {
        if (debug) {
            DebugMessagesEntity debugMessagesEntity = DebugMessagesEntity.builder()
                    .request(requestEntity)
                    .text(String.join("\n", translator.getDebugMessages()))
                    .build();
            return debugMessagesService.save(debugMessagesEntity).thenReturn(requestEntity);
        }
        return Mono.just(requestEntity);
    }

    private Mono<RequestEntity> saveCompilerOut(RequestEntity requestEntity, ProgramAndDataDto out) {
        byte[] data = ArrayUtils.toPrimitive(out.data().stream().flatMap(Arrays::stream).toArray(Byte[]::new));
        byte[] program = ArrayUtils.toPrimitive(out.program().stream().flatMap(Arrays::stream).toArray(Byte[]::new));
        CompilerOutEntity compilerOutEntity = CompilerOutEntity.builder()
                .request(requestEntity)
                .data(data)
                .program(program)
                .build();
        return compilerOutService.save(compilerOutEntity).thenReturn(requestEntity);
    }

    private Mono<RequestEntity> addRequestToUser(RequestEntity requestEntity, String username) {
        Mono<RoleEntity> roleUserMono = userServiceClient.findRoleByName(RoleConstants.USER).map(roleEntityMapper::fromDTO);
        return userServiceClient.findUserByUsername(username)
                .map(userEntityMapper::fromDTO)
                .flatMap(userEntity -> roleUserMono.flatMap(roleUser -> {
                    if (userEntity.getRoles().size() == 1 && userEntity.getRoles().contains(roleUser) && !userEntity.getRequests().isEmpty()) {
                        RequestEntity oldRequestEntity = userEntity.getRequests().get(0);
                        userEntity.getRequests().clear();
                        userEntity.getRequests().add(requestEntity);
                        return requestService.delete(oldRequestEntity).then(Mono.just(userEntity));
                    }
                    userEntity.getRequests().add(requestEntity);
                    return Mono.just(userEntity);
                }))
                .map(userEntityMapper::toDTO)
                .flatMap(userServiceClient::saveUser)
                .thenReturn(requestEntity);
    }

    @Override
    public Mono<Boolean> checkRequestOwnedByUser(Authentication authentication, Long requestId) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority(RoleConstants.ADMIN))) {
            return Mono.just(true);
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority(RoleConstants.VIP))) {
            return userServiceClient.findUserByUsername(authentication.getName())
                    .map(userEntityMapper::fromDTO)
                    .flatMap(userEntity -> requestService.findById(requestId).map(userEntity.getRequests()::contains))
                    .onErrorMap(NoSuchElementException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
        }
        return Mono.just(false);
    }
}