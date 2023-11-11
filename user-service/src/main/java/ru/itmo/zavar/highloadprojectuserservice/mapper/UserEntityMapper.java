package ru.itmo.zavar.highloadprojectuserservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadprojectuserservice.dto.inner.UserDTO;
import ru.itmo.zavar.highloadprojectuserservice.entity.security.UserEntity;

@Mapper(componentModel = "spring")
public interface UserEntityMapper {
    UserEntity fromDTO(UserDTO dto);

    UserDTO toDTO(UserEntity entity);
}
