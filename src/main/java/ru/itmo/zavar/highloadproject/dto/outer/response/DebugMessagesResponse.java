package ru.itmo.zavar.highloadproject.dto.outer.response;

import lombok.Builder;

@Builder
public record DebugMessagesResponse(Long id, String[] text) {
}
