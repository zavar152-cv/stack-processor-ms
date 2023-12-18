package ru.itmo.zavar.highload.notificationservice.service;

import ru.itmo.zavar.highload.notificationservice.dto.NotificationRequest;

public interface NotificationSender {
    void sendSimpleMessage(NotificationRequest notificationRequest);
}
