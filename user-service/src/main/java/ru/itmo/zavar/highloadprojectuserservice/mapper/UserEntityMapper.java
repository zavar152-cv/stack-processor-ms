package ru.itmo.zavar.highloadprojectuserservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadprojectuserservice.dto.inner.request.UserEntityRequest;
import ru.itmo.zavar.highloadprojectuserservice.dto.inner.response.UserEntityResponse;
import ru.itmo.zavar.highloadprojectuserservice.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromRequest(UserEntityRequest request);
    UserEntity fromResponse(UserEntityResponse response); // TODO: убрать
    UserEntityRequest toRequest(UserEntity entity); // TODO: убрать
    UserEntityResponse toResponse(UserEntity entity);
}
