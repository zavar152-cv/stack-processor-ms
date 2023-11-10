package ru.itmo.zavar.highloadproject.authservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.authservice.dto.inner.request.UserEntityRequest;
import ru.itmo.zavar.highloadproject.authservice.dto.inner.response.UserEntityResponse;
import ru.itmo.zavar.highloadproject.authservice.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromRequest(UserEntityRequest request);
    UserEntity fromResponse(UserEntityResponse response);
    UserEntityRequest toRequest(UserEntity entity);
    UserEntityResponse toResponse(UserEntity entity);
}
