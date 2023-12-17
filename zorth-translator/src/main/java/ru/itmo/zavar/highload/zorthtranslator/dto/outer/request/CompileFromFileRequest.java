package ru.itmo.zavar.highload.zorthtranslator.dto.outer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record CompileFromFileRequest(@Schema(example = "true") @NotNull Boolean debug,
                                     @NotNull @Positive Long fileId) {
}
