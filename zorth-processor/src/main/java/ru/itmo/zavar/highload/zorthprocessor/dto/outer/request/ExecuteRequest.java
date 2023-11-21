package ru.itmo.zavar.highload.zorthprocessor.dto.outer.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ExecuteRequest(@NotNull @NotEmpty String[] input, @NotNull Long requestId) {
}
