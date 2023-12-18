package ru.itmo.zavar.highload.notificationservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import ru.itmo.zavar.highload.notificationservice.dto.NotificationRequest;
import ru.itmo.zavar.highload.notificationservice.service.NotificationSender;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSenderImpl implements NotificationSender {

    private final JavaMailSender emailSender;

    @KafkaListener(topics = "notification", groupId = "mail", containerFactory = "notificationRequestKafkaListenerContainerFactory")
    @Override
    public void sendSimpleMessage(NotificationRequest notificationRequest) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@highload.ru");
        message.setTo(notificationRequest.to());
        message.setSubject(notificationRequest.subject());
        message.setText(notificationRequest.text());
        emailSender.send(message);
    }
}
