package ru.itmo.zavar.highloadprojectrequestservice.mapper;

import org.mapstruct.Mapper;
import ru.itmo.zavar.highloadprojectrequestservice.dto.inner.request.RequestServiceRequest;
import ru.itmo.zavar.highloadprojectrequestservice.dto.inner.response.RequestServiceResponse;
import ru.itmo.zavar.highloadprojectrequestservice.entity.zorth.RequestEntity;

@Mapper(componentModel = "spring")
public interface RequestEntityMapper {
    RequestEntity fromRequest(RequestServiceRequest request);
    RequestEntity fromResponse(RequestServiceResponse response);
    RequestServiceRequest toRequest(RequestEntity entity);
    RequestServiceResponse toResponse(RequestEntity entity);
}
