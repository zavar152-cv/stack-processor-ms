package ru.itmo.zavar.highloadproject.dto.request;

import lombok.Builder;

@Builder
public record CompileRequest(boolean debug, String text) {
}
