package ru.itmo.zavar.highload.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.response.CompileResponse;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

@Mapper(componentModel = "spring")
public interface RequestEntityMapper {
    CompileResponse toDTO(RequestEntity entity);
}
