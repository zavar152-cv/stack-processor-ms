package ru.itmo.zavar.highloadproject.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.inner.UserDTO;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromDTO(UserDTO dto);

    UserDTO toDTO(UserEntity entity);
}
