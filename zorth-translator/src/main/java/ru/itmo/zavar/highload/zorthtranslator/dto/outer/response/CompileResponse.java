package ru.itmo.zavar.highload.zorthtranslator.dto.outer.response;

import lombok.Builder;

@Builder
public record CompileResponse(Long id, String text, Boolean debug) {
}