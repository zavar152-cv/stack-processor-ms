package ru.itmo.zavar.highloadprojectuserservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadprojectuserservice.dto.inner.request.UserServiceRequest;
import ru.itmo.zavar.highloadprojectuserservice.dto.inner.response.UserServiceResponse;
import ru.itmo.zavar.highloadprojectuserservice.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromRequest(UserServiceRequest request);
    UserEntity fromResponse(UserServiceResponse response);
    UserServiceRequest toRequest(UserEntity entity);
    UserServiceResponse toResponse(UserEntity entity);
}
