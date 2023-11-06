package ru.itmo.zavar.highloadprojectrequestservice.service;

import ru.itmo.zavar.highloadprojectrequestservice.entity.zorth.RequestEntity;

import java.util.Optional;

public interface RequestService {
    RequestEntity saveRequest(RequestEntity requestEntity);

    void deleteRequest(RequestEntity requestEntity);

    Optional<RequestEntity> findById(Long id);
}
