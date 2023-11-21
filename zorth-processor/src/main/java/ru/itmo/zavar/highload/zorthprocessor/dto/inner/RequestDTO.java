package ru.itmo.zavar.highload.zorthprocessor.dto.inner;

import lombok.Builder;

@Builder
public record RequestDTO(Long id, String text, Boolean debug) {
}