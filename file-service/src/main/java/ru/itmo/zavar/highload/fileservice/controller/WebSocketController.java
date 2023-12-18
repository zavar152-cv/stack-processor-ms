package ru.itmo.zavar.highload.fileservice.controller;

import jakarta.annotation.Nullable;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import ru.itmo.zavar.highload.fileservice.dto.response.FileContentResponse;
import ru.itmo.zavar.highload.fileservice.dto.response.FileInfoResponse;
import ru.itmo.zavar.highload.fileservice.service.StorageService;
import ru.itmo.zavar.highload.fileservice.util.WsFileRequestMessage;
import ru.itmo.zavar.highload.fileservice.util.WsFileResponseMessage;

import java.lang.reflect.Type;
import java.util.NoSuchElementException;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController implements StompSessionHandler {

    @Value("${ws-server}")
    private String wsServerUrl;
    @Value("${destination-topic}")
    private String destinationTopic;
    @Value("${subscribe-topic}")
    private String subscribeTopic;
    private StompSession stompSession;
    private final StorageService storageService;

    @EventListener(value = ApplicationReadyEvent.class)
    public void connect() {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);
        // alternative: stompClient.setMessageConverter(new StringMessageConverter());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        try {
            stompSession = stompClient.connectAsync(wsServerUrl, this).get();
            log.info("Connected to WS server");
        } catch (Exception e) {
            log.error("Connection to WS server failed {}", e.getMessage());
        }
    }

    @Override
    public void afterConnected(@Nullable StompSession session, @Nullable StompHeaders connectedHeaders) {
        log.info("Connection to STOMP server established.\n" +
                "Session: " + session + "\n" +
                "Headers: " + connectedHeaders + "\n");
        subscribe(subscribeTopic);
    }

    public void subscribe(String topicId) {
        log.info("Subscribing to topic: {}", topicId);
        stompSession.subscribe(topicId, this);
    }

    @Override
    public void handleFrame(@Nullable StompHeaders headers, Object payload) {
        WsFileRequestMessage requestMessage = (WsFileRequestMessage) payload;
        log.info("Received message:" + requestMessage);
        try {
            FileInfoResponse fileInfoResponse = storageService.loadInfoById(requestMessage.getFileId());
            if(!fileInfoResponse.ownerName().equals(requestMessage.getUsername())) {
                stompSession.send(destinationTopic + "/" + requestMessage.getClientId(), new WsFileResponseMessage(true, false, "", ""));
            } else {
                FileContentResponse fileContentResponse = storageService.loadContentById(requestMessage.getFileId());
                stompSession.send(destinationTopic + "/" + requestMessage.getClientId(), new WsFileResponseMessage(true, true, fileContentResponse.name(), fileContentResponse.content()));
            }
        } catch (NoSuchElementException e) {
            stompSession.send(destinationTopic + "/" + requestMessage.getClientId(), new WsFileResponseMessage(false, false, "", ""));
        }
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
        return WsFileRequestMessage.class;
    }

    @PreDestroy
    void onShutDown() {
        if (stompSession != null) {
            stompSession.disconnect();
        }
    }
}
