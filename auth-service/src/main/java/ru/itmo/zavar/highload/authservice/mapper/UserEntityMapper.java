package ru.itmo.zavar.highload.authservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highload.authservice.dto.inner.UserDTO;
import ru.itmo.zavar.highload.authservice.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromDTO(UserDTO response);
}
