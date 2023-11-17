package ru.itmo.zavar.highloadproject.dto.outer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PipelineRequest(@NotNull boolean debug, @NotBlank String text, @NotNull String[] input) {
}
