package ru.itmo.zavar.highloadproject.dto.response;

import lombok.Builder;

@Builder
public record RequestResponse(Long id, String[] text, boolean debug) {
}
