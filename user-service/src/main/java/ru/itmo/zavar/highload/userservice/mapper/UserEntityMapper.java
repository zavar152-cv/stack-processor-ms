package ru.itmo.zavar.highload.userservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highload.userservice.dto.inner.UserDTO;
import ru.itmo.zavar.highload.userservice.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromDTO(UserDTO dto);

    UserDTO toDTO(UserEntity entity);
}
