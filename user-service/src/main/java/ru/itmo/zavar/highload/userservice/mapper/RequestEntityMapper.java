package ru.itmo.zavar.highload.userservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highload.userservice.dto.outer.response.GetRequestsResponse;
import ru.itmo.zavar.highload.userservice.entity.zorth.RequestEntity;

@Mapper(componentModel = "spring")
public interface RequestEntityMapper {
    GetRequestsResponse toDTO(RequestEntity entity);
}
