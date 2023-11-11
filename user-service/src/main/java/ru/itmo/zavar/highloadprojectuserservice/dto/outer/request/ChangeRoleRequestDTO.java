package ru.itmo.zavar.highloadprojectuserservice.dto.outer.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record ChangeRoleRequestDTO(@NotBlank @Size(min = 5, max = 25) String username, @NotBlank String role) {
}
