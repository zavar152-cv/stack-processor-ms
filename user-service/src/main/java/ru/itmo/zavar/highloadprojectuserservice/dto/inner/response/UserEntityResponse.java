package ru.itmo.zavar.highloadprojectuserservice.dto.inner.response;

import lombok.Builder;
import ru.itmo.zavar.highloadprojectuserservice.entity.security.RoleEntity;
import ru.itmo.zavar.highloadprojectuserservice.entity.zorth.RequestEntity;

import java.util.List;
import java.util.Set;

@Builder
public record UserEntityResponse(Long id, String username, String password, Set<RoleEntity> roles, List<RequestEntity> requests) {
}
