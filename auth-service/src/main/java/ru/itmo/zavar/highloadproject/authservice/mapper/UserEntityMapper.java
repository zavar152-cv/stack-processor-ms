package ru.itmo.zavar.highloadproject.authservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.authservice.dto.inner.UserDTO;
import ru.itmo.zavar.highloadproject.authservice.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromDTO(UserDTO response);
}
