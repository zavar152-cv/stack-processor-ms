package ru.itmo.zavar.highload.notificationservice.dto;

import lombok.Builder;

@Builder
public record NotificationRequest(String to, String subject, String text) {
}
