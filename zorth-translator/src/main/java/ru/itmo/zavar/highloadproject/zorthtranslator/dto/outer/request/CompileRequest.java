package ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CompileRequest(@NotNull Boolean debug, @NotBlank String text) {
}
