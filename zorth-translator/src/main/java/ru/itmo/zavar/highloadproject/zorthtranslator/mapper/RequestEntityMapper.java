package ru.itmo.zavar.highloadproject.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.inner.RequestDTO;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.RequestEntity;

@Mapper(componentModel = "spring")
public interface RequestEntityMapper {
    RequestEntity fromDTO(RequestDTO dto);
    RequestDTO toDTO(RequestEntity entity);
}
