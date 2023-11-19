package ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.response;

import lombok.Builder;

@Builder
public record GetDebugMessagesResponse(Long id, String[] text) {
}
