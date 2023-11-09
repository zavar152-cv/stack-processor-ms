package ru.itmo.zavar.highloadprojectauthservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadprojectauthservice.dto.inner.request.UserEntityRequest;
import ru.itmo.zavar.highloadprojectauthservice.dto.inner.response.UserEntityResponse;
import ru.itmo.zavar.highloadprojectauthservice.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromRequest(UserEntityRequest request);
    UserEntity fromResponse(UserEntityResponse response);
    UserEntityRequest toRequest(UserEntity entity);
    UserEntityResponse toResponse(UserEntity entity);
}
