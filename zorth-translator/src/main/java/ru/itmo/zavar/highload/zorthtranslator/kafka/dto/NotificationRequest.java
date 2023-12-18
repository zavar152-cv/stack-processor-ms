package ru.itmo.zavar.highload.zorthtranslator.kafka.dto;

import lombok.Builder;

@Builder
public record NotificationRequest(String to, String subject, String text) {
}
