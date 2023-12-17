package ru.itmo.zavar.highload.zorthtranslator.dto.outer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CompileRequest(@Schema(example = "true") @NotNull Boolean debug,
                             @Schema(example = "variable a\n3 a !\na @") @NotBlank String text) {
}
