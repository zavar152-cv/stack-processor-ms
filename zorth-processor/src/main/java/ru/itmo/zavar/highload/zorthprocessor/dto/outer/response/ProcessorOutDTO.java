package ru.itmo.zavar.highload.zorthprocessor.dto.outer.response;

import lombok.Builder;

@Builder
public record ProcessorOutDTO(Long id, String input, String[] tickLogs) {
}
