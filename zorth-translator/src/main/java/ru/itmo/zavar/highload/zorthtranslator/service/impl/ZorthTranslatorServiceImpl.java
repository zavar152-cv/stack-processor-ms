package ru.itmo.zavar.highload.zorthtranslator.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highload.zorthtranslator.client.UserServiceClient;
import ru.itmo.zavar.highload.zorthtranslator.entity.security.RoleEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.security.UserEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highload.zorthtranslator.mapper.RoleEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.mapper.UserEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.service.CompilerOutService;
import ru.itmo.zavar.highload.zorthtranslator.service.DebugMessagesService;
import ru.itmo.zavar.highload.zorthtranslator.service.RequestService;
import ru.itmo.zavar.highload.zorthtranslator.service.ZorthTranslatorService;
import ru.itmo.zavar.highload.zorthtranslator.util.RoleConstants;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.zorth.ProgramAndDataDto;
import ru.itmo.zavar.zorth.ZorthTranslator;

import java.util.ArrayList;
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
    public RequestEntity compileAndLinkage(boolean debug, String text, String username)
            throws ZorthException, NoSuchElementException, DataAccessException, ResponseStatusException {
        /* Вызываем транслятор */
        ZorthTranslator translator = new ZorthTranslator(null, null, true);
        translator.compileFromString(debug, text);
        translator.linkage(debug);
        ProgramAndDataDto out = translator.getCompiledProgramAndDataInBytes();

        /* Сохраняем запрос в бд */
        RequestEntity requestEntity = RequestEntity.builder().debug(debug).text(text).build();
        requestEntity = requestService.save(requestEntity);

        /* Если debug включен, то сохраняем его в бд */
        if (debug) {
            DebugMessagesEntity debugMessagesEntity = DebugMessagesEntity.builder()
                    .request(requestEntity)
                    .text(String.join("\n", translator.getDebugMessages()))
                    .build();
            debugMessagesService.save(debugMessagesEntity);
        }

        /* Сохраняем ответ компилятора в бд */
        ArrayList<Byte> data = new ArrayList<>();
        out.data().forEach(bytes -> data.addAll(Arrays.stream(bytes).toList()));
        Byte[] dataArray = new Byte[data.size()];
        data.toArray(dataArray);

        ArrayList<Byte> program = new ArrayList<>();
        out.program().forEach(bytes -> program.addAll(Arrays.stream(bytes).toList()));
        Byte[] programArray = new Byte[program.size()];
        program.toArray(programArray);

        CompilerOutEntity compilerOutEntity = CompilerOutEntity.builder()
                .request(requestEntity)
                .data(ArrayUtils.toPrimitive(dataArray))
                .program(ArrayUtils.toPrimitive(programArray))
                .build();
        compilerOutService.save(compilerOutEntity);

        /* Получаем пользователя и роль "USER" из бд. Если пользователь обычный, у него может быть только 1 запрос.
         * Надо удалить прошлый, если он есть. */
        UserEntity userEntity = userEntityMapper.fromDTO(userServiceClient.findUserByUsername(username).getBody());
        RoleEntity roleUser = roleEntityMapper.fromDTO(userServiceClient.findRoleByName(RoleConstants.USER).getBody());
        if (userEntity.getRoles().size() == 1 && userEntity.getRoles().contains(roleUser) && !userEntity.getRequests().isEmpty()) {
            RequestEntity oldRequestEntity = userEntity.getRequests().get(0);
            userEntity.getRequests().clear();
            requestService.delete(oldRequestEntity);
        }

        /* Сохраняем пользователя в бд */
        userEntity.getRequests().add(requestEntity);
        userServiceClient.saveUser(userEntityMapper.toDTO(userEntity));

        return requestEntity;
    }

    @Override
    public boolean checkRequestOwnedByUser(String username, Long requestId) throws ResponseStatusException {
        try {
            UserEntity userEntity = userEntityMapper.fromDTO(userServiceClient.findUserByUsername(username).getBody());
            RequestEntity requestEntity = requestService.findById(requestId);
            return userEntity.getRequests().contains(requestEntity);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}