package ru.itmo.zavar.highloadproject.userservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.userservice.dto.inner.RoleDTO;
import ru.itmo.zavar.highloadproject.userservice.entity.security.RoleEntity;

@Mapper(componentModel = "spring")
public interface RoleEntityMapper {
    RoleDTO toDTO(RoleEntity entity);
}
