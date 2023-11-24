package ru.itmo.zavar.highload.zorthprocessor.dto.outer.response;

import lombok.Builder;

@Builder
public record GetProcessorOutResponse(Long id, String input, String[] tickLogs) {
}
