package ru.itmo.zavar.highloadprojectrequestservice.dto.inner.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record RequestServiceResponse(@NotNull Long id, @NotBlank String text, @NotNull Boolean debug) {
}
