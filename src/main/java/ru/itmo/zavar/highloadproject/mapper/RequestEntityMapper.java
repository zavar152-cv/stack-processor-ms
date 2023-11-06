package ru.itmo.zavar.highloadproject.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadproject.dto.inner.request.RequestServiceRequest;
import ru.itmo.zavar.highloadproject.dto.inner.response.RequestServiceResponse;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;

@Mapper(componentModel = "spring")
public interface RequestEntityMapper {
    RequestEntity fromRequest(RequestServiceRequest request);
    RequestEntity fromResponse(RequestServiceResponse response);
    RequestServiceRequest toRequest(RequestEntity entity);
    RequestServiceResponse toResponse(RequestEntity entity);
}
