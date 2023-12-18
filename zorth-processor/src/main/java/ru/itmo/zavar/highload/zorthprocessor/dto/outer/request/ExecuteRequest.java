package ru.itmo.zavar.highload.zorthprocessor.dto.outer.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ExecuteRequest(@Schema(example = "[\"k\",\"2\",\"3\"]") @NotNull @NotEmpty String[] input,
                             @Schema(example = "1") @NotNull Long requestId) {
}
