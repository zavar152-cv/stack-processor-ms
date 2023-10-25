package ru.itmo.zavar.highloadproject.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RequestResponse(Long id, String[] text, boolean debug) {
}
