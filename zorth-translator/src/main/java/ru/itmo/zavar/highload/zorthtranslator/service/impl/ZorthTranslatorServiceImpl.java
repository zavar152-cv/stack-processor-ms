package ru.itmo.zavar.highload.zorthtranslator.service.impl;

import jakarta.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthtranslator.client.UserServiceClient;
import ru.itmo.zavar.highload.zorthtranslator.entity.security.RoleEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highload.zorthtranslator.kafka.dto.NotificationRequest;
import ru.itmo.zavar.highload.zorthtranslator.mapper.RoleEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.mapper.UserEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.service.CompilerOutService;
import ru.itmo.zavar.highload.zorthtranslator.service.DebugMessagesService;
import ru.itmo.zavar.highload.zorthtranslator.service.RequestService;
import ru.itmo.zavar.highload.zorthtranslator.service.ZorthTranslatorService;
import ru.itmo.zavar.highload.zorthtranslator.util.RoleConstants;
import ru.itmo.zavar.highload.zorthtranslator.util.WsFileRequestMessage;
import ru.itmo.zavar.highload.zorthtranslator.util.WsFileResponseMessage;
import ru.itmo.zavar.zorth.ProgramAndDataDto;
import ru.itmo.zavar.zorth.ZorthTranslator;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZorthTranslatorServiceImpl implements ZorthTranslatorService, StompSessionHandler {
    private final RequestService requestService;
    private final CompilerOutService compilerOutService;
    private final DebugMessagesService debugMessagesService;

    @Value("${ws-server}")
    private String wsServerUrl;
    @Value("${destination-topic}")
    private String destinationTopic;
    @Value("${subscribe-topic}")
    private String subscribeTopic;
    private StompSession stompSession;
    private WsFileResponseMessage wsFileResponseMessage;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    private final UserServiceClient userServiceClient;
    private final UserEntityMapper userEntityMapper;
    private final RoleEntityMapper roleEntityMapper;

    @EventListener(value = ApplicationReadyEvent.class)
    public void connect() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        try {
            stompSession = stompClient.connectAsync(wsServerUrl, this).get();
            log.info("Connected to WS server");
        } catch (Exception e) {
            log.error("Connection to WS server failed {}", e.getMessage());
        }
    }

    @Override
    public Mono<RequestEntity> compileAndLinkageFromFile(boolean debug, String username, Long fileId, String email) throws InterruptedException, EntityNotFoundException, IllegalArgumentException {
        WsFileRequestMessage wsFileRequestMessage = new WsFileRequestMessage(stompSession.getSessionId(), username, fileId);
        stompSession.send(destinationTopic, wsFileRequestMessage);
        countDownLatch.await(); //waiting for file-service response, bruh
        if(!wsFileResponseMessage.getExists())
            throw new EntityNotFoundException("File not found");
        if(!wsFileResponseMessage.getOwned())
            throw new IllegalArgumentException("You don't have permissions to access this file");
        String text = wsFileResponseMessage.getContent();
        return compileAndLinkage(debug, text, username, email);
    }

    @Override
    public Mono<RequestEntity> compileAndLinkage(boolean debug, String text, String username, String email) {
        /* Вызываем транслятор */
        ZorthTranslator translator = new ZorthTranslator(null, null, true);
        translator.compileFromString(debug, text);
        translator.linkage(debug);
        ProgramAndDataDto out = translator.getCompiledProgramAndDataInBytes();

        kafkaTemplate.send("notification", new NotificationRequest(email, "Compilation completed",
                "Compilation completed for:\n" + text));

        /* Сохраняем всё в бд */
        RequestEntity requestEntity = RequestEntity.builder().debug(debug).text(text).build();
        return requestService.save(requestEntity)
                .flatMap(savedRequestEntity -> saveDebugMessages(savedRequestEntity, debug, translator))
                .flatMap(savedRequestEntity -> saveCompilerOut(savedRequestEntity, out))
                .flatMap(savedRequestEntity -> addRequestToUser(savedRequestEntity, username))
                .onErrorResume(e -> requestService.delete(requestEntity).then(Mono.error(e)));
    }

    @Override
    public Mono<RequestEntity> compileAndLinkageFromFile(boolean debug, String username, Long fileId) throws InterruptedException, EntityNotFoundException, IllegalArgumentException {
        WsFileRequestMessage wsFileRequestMessage = new WsFileRequestMessage(stompSession.getSessionId(), username, fileId);
        stompSession.send(destinationTopic, wsFileRequestMessage);
        countDownLatch.await(); //waiting for file-service response, bruh
        if(!wsFileResponseMessage.getExists())
            throw new EntityNotFoundException("File not found");
        if(!wsFileResponseMessage.getOwned())
            throw new IllegalArgumentException("You don't have permissions to access this file");
        String text = wsFileResponseMessage.getContent();
        return compileAndLinkage(debug, text, username);
    }

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
                .flatMap(savedRequestEntity -> addRequestToUser(savedRequestEntity, username))
                .onErrorResume(e -> requestService.delete(requestEntity).then(Mono.error(e)));
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
        Mono<RoleEntity> roleUserMono = userServiceClient.getRole(RoleConstants.USER).map(roleEntityMapper::fromDTO);
        return userServiceClient.getUser(username)
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
            return userServiceClient.getUser(authentication.getName())
                    .map(userEntityMapper::fromDTO)
                    .flatMap(userEntity -> requestService.findById(requestId).map(userEntity.getRequests()::contains))
                    .onErrorMap(NoSuchElementException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
        }
        return Mono.just(false);
    }

    @Override
    public void afterConnected(@Nullable StompSession session, @Nullable StompHeaders connectedHeaders) {
        log.info("Connection to STOMP server established.\n" +
                "Session: " + session + "\n" +
                "Headers: " + connectedHeaders + "\n");
        subscribe(subscribeTopic + "/" + stompSession.getSessionId());
    }

    public void subscribe(String topicId) {
        log.info("Subscribing to topic: {}", topicId);
        stompSession.subscribe(topicId, this);
    }

    @Override
    public void handleFrame(@Nullable StompHeaders headers, Object payload) {
        WsFileResponseMessage p = (WsFileResponseMessage) payload;
        log.info("Received message:" + p);
        wsFileResponseMessage = p;
        countDownLatch.countDown();
        countDownLatch = new CountDownLatch(1);
    }

    @Override
    public void handleException(@Nullable StompSession session, StompCommand command, @Nullable StompHeaders headers, @Nullable byte[] payload, @Nullable Throwable exception) {
    }

    @Override
    public void handleTransportError(StompSession session, @Nullable Throwable exception) {
        log.error("Retrieved a transport error: {}", exception == null ? "" : exception.getMessage());
    }

    @Override
    public Type getPayloadType(@Nullable StompHeaders headers) {
        return WsFileResponseMessage.class;
    }

    @PreDestroy
    void onShutDown() {
        if (stompSession != null) {
            stompSession.disconnect();
        }
    }
}