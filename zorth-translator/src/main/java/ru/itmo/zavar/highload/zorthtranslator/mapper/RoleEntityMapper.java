package ru.itmo.zavar.highload.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highload.zorthtranslator.dto.inner.RoleDTO;
import ru.itmo.zavar.highload.zorthtranslator.entity.security.RoleEntity;

@Mapper(componentModel = "spring")
public interface RoleEntityMapper {
    RoleEntity fromDTO(RoleDTO dto);
}
