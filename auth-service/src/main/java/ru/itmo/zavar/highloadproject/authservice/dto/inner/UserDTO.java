package ru.itmo.zavar.highloadproject.authservice.dto.inner;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.itmo.zavar.highloadproject.authservice.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.authservice.entity.zorth.RequestEntity;

import java.util.List;
import java.util.Set;

@Builder
public record UserDTO(@NotNull Long id, @NotBlank String username, @NotBlank String password,
                      @NotNull Set<RoleEntity> roles, List<RequestEntity> requests) {
}