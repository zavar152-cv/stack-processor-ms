package ru.itmo.zavar.highloadproject.dto.inner.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.itmo.zavar.highloadproject.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;

import java.util.List;
import java.util.Set;

@Builder
public record UserServiceResponse(@NotNull Long id, @NotBlank String username, @NotBlank String password, @NotNull Set<RoleEntity> roles, @NotNull List<RequestEntity> requests) {
}
