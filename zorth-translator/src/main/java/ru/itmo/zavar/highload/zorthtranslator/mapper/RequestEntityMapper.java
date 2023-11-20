package ru.itmo.zavar.highload.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.RequestDTO;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

@Mapper(componentModel = "spring")
public interface RequestEntityMapper {
    RequestDTO toDTO(RequestEntity entity);
}
