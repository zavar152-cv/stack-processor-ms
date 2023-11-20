package ru.itmo.zavar.highloadproject.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.outer.RequestDTO;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.RequestEntity;

@Mapper(componentModel = "spring")
public interface RequestEntityMapper {
    RequestDTO toDTO(RequestEntity entity);
}
