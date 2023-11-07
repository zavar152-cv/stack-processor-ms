package ru.itmo.zavar.highloadprojectuserservice.dto.inner.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.itmo.zavar.highloadprojectuserservice.entity.security.RoleEntity;
import ru.itmo.zavar.highloadprojectuserservice.entity.zorth.RequestEntity;

import java.util.List;
import java.util.Set;

@Builder
public record UserServiceResponse(@NotNull Long id, @NotBlank String username, @NotBlank String password, @NotNull Set<RoleEntity> roles, @NotNull List<RequestEntity> requests) {
}
