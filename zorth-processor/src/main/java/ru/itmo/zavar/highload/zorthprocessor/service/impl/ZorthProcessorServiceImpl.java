package ru.itmo.zavar.highload.zorthprocessor.service.impl;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.comp.ControlUnit;
import ru.itmo.zavar.exception.ControlUnitException;
import ru.itmo.zavar.highload.zorthprocessor.client.ZorthTranslatorClient;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.CompileRequest;
import ru.itmo.zavar.highload.zorthprocessor.entity.zorth.ProcessorOutEntity;
import ru.itmo.zavar.highload.zorthprocessor.service.ProcessorOutService;
import ru.itmo.zavar.highload.zorthprocessor.service.ZorthProcessorService;
import ru.itmo.zavar.highload.zorthprocessor.util.RoleConstants;

import java.util.ArrayList;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ZorthProcessorServiceImpl implements ZorthProcessorService {
    private final ProcessorOutService processorOutService;
    private final ZorthTranslatorClient zorthTranslatorClient;

    @Override
    public Mono<ProcessorOutEntity> pipeline(boolean debug, String text, String[] input, Authentication authentication) {
        String username = authentication.getName();
        String authorities = extractAuthorities(authentication);
        CompileRequest compileRequest = CompileRequest.builder().debug(debug).text(text).build();
        return zorthTranslatorClient.compile(compileRequest, username, authorities)
                .flatMap(dto -> startProcessorAndGetLogs(input, dto.id(), authentication));
    }

    @Override
    public Mono<ProcessorOutEntity> startProcessorAndGetLogs(String[] input, Long requestId, Authentication authentication) {
        String username = authentication.getName();
        String authorities = extractAuthorities(authentication);
        return zorthTranslatorClient.getCompilerOutOfRequest(requestId, username, authorities).flatMap(dto -> {
            try {
                Gson gson = new Gson();
                String json = gson.toJson(input);
                JSONParser jsonParser = new JSONParser();
                JSONArray inputJson = (JSONArray) jsonParser.parse(json);

                ControlUnit controlUnit = new ControlUnit(dto.program(), dto.data(), inputJson, true);
                controlUnit.start();

                StringBuilder stringBuilder = new StringBuilder();
                controlUnit.getTickLog().forEach(tickLog -> {
                    stringBuilder.append("\n");
                    stringBuilder.append(tickLog.toString());
                });

                ProcessorOutEntity processorOutEntity = ProcessorOutEntity.builder()
                        .tickLogs(stringBuilder.toString().getBytes())
                        .compilerOutId(dto.id())
                        .input(inputJson.toString().getBytes())
                        .build();
                return processorOutService.save(processorOutEntity);
            } catch (ParseException e) {
                return Mono.error(e);
            }
        });
    }

    @Override
    public Flux<ProcessorOutEntity> findAllProcessorOutByRequestId(Long requestId, Authentication authentication) {
        String username = authentication.getName();
        String authorities = extractAuthorities(authentication);
        return zorthTranslatorClient.getCompilerOutOfRequest(requestId, username, authorities)
                .flatMapMany(dto -> processorOutService.findAllByCompilerOutId(dto.id()));
    }

    private String extractAuthorities(Authentication authentication) {
        ArrayList<String> authoritiesAsList = new ArrayList<>(authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        /* Чтобы у обычных пользователей тоже была возможность получить ответ, но только при условии,
         * что запрос принадлежит им, добавляем в запрос роль VIP. */
        if ((authoritiesAsList.size() == 1) && authoritiesAsList.contains(RoleConstants.USER)) {
            authoritiesAsList.add(RoleConstants.VIP);
        }
        return authoritiesAsList.stream().reduce("", (a, b) -> a + "," + b);
    }
}
