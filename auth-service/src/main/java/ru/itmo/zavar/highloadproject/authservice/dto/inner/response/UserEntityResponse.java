package ru.itmo.zavar.highloadproject.authservice.dto.inner.response;

import lombok.Builder;
import ru.itmo.zavar.highloadproject.authservice.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.authservice.entity.zorth.RequestEntity;

import java.util.List;
import java.util.Set;

@Builder
public record UserEntityResponse(Long id, String username, String password, Set<RoleEntity> roles, List<RequestEntity> requests) {
}
