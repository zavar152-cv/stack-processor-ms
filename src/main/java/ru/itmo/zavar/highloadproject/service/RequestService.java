package ru.itmo.zavar.highloadproject.service;

import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;

import java.util.Optional;

public interface RequestService {
    RequestEntity saveRequest(RequestEntity requestEntity);

    void deleteRequest(RequestEntity requestEntity);

    Optional<RequestEntity> findById(Long id);
}
