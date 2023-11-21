package ru.itmo.zavar.highload.zorthprocessor.dto.inner;

import lombok.Builder;

import java.util.ArrayList;

@Builder
public record CompilerOutDTO(Long id, Long requestId, ArrayList<Long> program, ArrayList<Long> data) {
}