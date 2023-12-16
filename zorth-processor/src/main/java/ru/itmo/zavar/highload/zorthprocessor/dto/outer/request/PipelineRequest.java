package ru.itmo.zavar.highload.zorthprocessor.dto.outer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PipelineRequest(@Schema(example = "true") @NotNull Boolean debug,
                              @Schema(example = "variable a\n3 a !\na @") @NotBlank String text,
                              @Schema(example = "[\"k\",\"2\",\"3\"]") @NotNull String[] input) {
}
