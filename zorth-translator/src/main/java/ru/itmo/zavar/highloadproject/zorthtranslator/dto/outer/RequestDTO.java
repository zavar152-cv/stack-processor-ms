package ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RequestDTO(Long id, @NotBlank String text, @NotNull Boolean debug) {
}