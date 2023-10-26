package ru.itmo.zavar.highloadproject.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ProcessorOutResponse(@NotNull Long id, @NotBlank String input, @NotNull String[] tickLogs) {
}
