package ru.itmo.zavar.highloadproject.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.dto.inner.request.UserServiceRequest;
import ru.itmo.zavar.highloadproject.dto.inner.response.UserServiceResponse;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromRequest(UserServiceRequest request);
    UserEntity fromResponse(UserServiceResponse response);
    UserServiceRequest toRequest(UserEntity entity);
    UserServiceResponse toResponse(UserEntity entity);
}
