package ru.itmo.zavar.highloadproject.zorthtranslator.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highloadproject.zorthtranslator.client.RequestServiceClient;
import ru.itmo.zavar.highloadproject.zorthtranslator.client.UserServiceClient;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.inner.RequestDTO;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.mapper.RequestEntityMapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.mapper.UserEntityMapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.CompilerOutService;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.DebugMessagesService;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.ZorthTranslatorService;
import ru.itmo.zavar.highloadproject.zorthtranslator.util.RoleConstants;
import ru.itmo.zavar.zorth.ProgramAndDataDto;
import ru.itmo.zavar.zorth.ZorthTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ZorthTranslatorServiceImpl implements ZorthTranslatorService {
    private final CompilerOutService compilerOutService;
    private final DebugMessagesService debugMessagesService;
    private final RequestServiceClient requestServiceClient;
    private final UserServiceClient userServiceClient;
    private final RequestEntityMapper requestEntityMapper;
    private final UserEntityMapper userEntityMapper;

    @Override
    @Transactional
    public RequestEntity compileAndLinkage(boolean debug, String text, String username)
            throws ZorthException, NoSuchElementException, DataAccessException, ResponseStatusException {
        /* Вызываем транслятор */
        ZorthTranslator translator = new ZorthTranslator(null, null, true);
        translator.compileFromString(debug, text);
        translator.linkage(debug);
        ProgramAndDataDto out = translator.getCompiledProgramAndDataInBytes();

        /* Сохраняем запрос в бд */
        RequestDTO requestDTO = RequestDTO.builder().debug(debug).text(text).build();
        RequestEntity requestEntity = requestEntityMapper.fromDTO(requestServiceClient.save(requestDTO).getBody());

        /* Получаем userEntity из бд, чтобы получить доступ к списку запросов пользователя.
         * Если пользователь обычный, у него может быть только 1 запрос. Надо удалить прошлый, если он есть. */
        UserEntity userEntity = userEntityMapper.fromDTO(userServiceClient.findUserByUsername(username).getBody());
        RoleEntity roleUser = new RoleEntity(3L, RoleConstants.USER); // TODO: вызов к userService
        if (userEntity.getRoles().size() == 1 && userEntity.getRoles().contains(roleUser) && !userEntity.getRequests().isEmpty()) {
            RequestEntity oldRequestEntity = userEntity.getRequests().get(0);
            compilerOutService.deleteByRequest(oldRequestEntity);
            debugMessagesService.deleteByRequest(oldRequestEntity);
            requestServiceClient.delete(oldRequestEntity.getId());
            userEntity.getRequests().clear();
        }

        /* Добавляем запрос в список запросов пользователя и сохраняем его */
        userEntity.getRequests().add(requestEntity);
        userServiceClient.saveUser(userEntityMapper.toDTO(userEntity));

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

        return requestEntity;
    }
}