package ru.itmo.zavar.highloadproject.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.inner.RoleDTO;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.security.RoleEntity;

@Mapper(componentModel = "spring")
public interface RoleEntityMapper {
    RoleEntity fromDTO(RoleDTO dto);
}
