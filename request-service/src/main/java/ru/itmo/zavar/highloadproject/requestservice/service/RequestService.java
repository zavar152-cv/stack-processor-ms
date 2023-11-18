package ru.itmo.zavar.highloadproject.requestservice.service;

import ru.itmo.zavar.highloadproject.requestservice.entity.zorth.RequestEntity;

public interface RequestService {
    RequestEntity save(RequestEntity requestEntity);

    RequestEntity findById(Long id);

    void delete(RequestEntity requestEntity);
}
