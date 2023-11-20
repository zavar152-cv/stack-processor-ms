package ru.itmo.zavar.highload.zorthtranslator.dto.outer.response;

import lombok.Builder;

@Builder
public record GetAllDebugMessagesResponse(Long id, Long requestId, String[] text) {
}
