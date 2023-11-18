package ru.itmo.zavar.highloadproject.requestservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.requestservice.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.requestservice.dto.inner.RequestDTO;

@Mapper(componentModel = "spring")
public interface RequestEntityMapper {
    RequestEntity fromDTO(RequestDTO dto);
    RequestDTO toDTO(RequestEntity entity);
}
