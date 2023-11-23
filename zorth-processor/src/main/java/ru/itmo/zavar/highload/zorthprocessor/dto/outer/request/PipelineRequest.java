package ru.itmo.zavar.highload.zorthprocessor.dto.outer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PipelineRequest(@NotNull Boolean debug, @NotBlank String text, @NotNull String[] input) {
}
