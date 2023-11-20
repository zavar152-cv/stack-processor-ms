package ru.itmo.zavar.highload.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highload.zorthtranslator.dto.inner.UserDTO;
import ru.itmo.zavar.highload.zorthtranslator.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromDTO(UserDTO dto);

    UserDTO toDTO(UserEntity entity);
}
