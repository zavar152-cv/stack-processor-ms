package ru.itmo.zavar.highloadproject.userservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.userservice.dto.inner.UserDTO;
import ru.itmo.zavar.highloadproject.userservice.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromDTO(UserDTO dto);

    UserDTO toDTO(UserEntity entity);
}
