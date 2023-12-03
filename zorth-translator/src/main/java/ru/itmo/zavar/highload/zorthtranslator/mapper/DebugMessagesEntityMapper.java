package ru.itmo.zavar.highload.zorthtranslator.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.response.GetDebugMessagesResponse;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

@Mapper(componentModel = "spring")
public interface DebugMessagesEntityMapper {
    @Mapping(source = "request", target = "requestId")
    GetDebugMessagesResponse toDTO(DebugMessagesEntity entity);

    default Long map(RequestEntity request) {
        return request == null ? null : request.getId();
    }

    default String[] map(String text) {
        return text == null ? null : text.split("\n");
    }
}
