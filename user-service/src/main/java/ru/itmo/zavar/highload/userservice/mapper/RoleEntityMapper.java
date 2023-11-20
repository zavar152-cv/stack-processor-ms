package ru.itmo.zavar.highload.userservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highload.userservice.entity.security.RoleEntity;
import ru.itmo.zavar.highload.userservice.dto.inner.RoleDTO;

@Mapper(componentModel = "spring")
public interface RoleEntityMapper {
    RoleDTO toDTO(RoleEntity entity);
}
