package ru.itmo.zavar.highload.zorthprocessor.dto.outer.response;

import lombok.Builder;

@Builder
public record CompileResponse(Long id, String text, Boolean debug) {
}