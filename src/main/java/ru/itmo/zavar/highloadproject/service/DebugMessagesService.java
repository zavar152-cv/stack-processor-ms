package ru.itmo.zavar.highloadproject.service;

import org.springframework.data.domain.Page;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;

import java.util.Optional;

public interface DebugMessagesService {
    void deleteDebugMessagesByRequest(RequestEntity requestEntity);

    DebugMessagesEntity saveDebugMessages(DebugMessagesEntity debugMessagesEntity);

    Optional<DebugMessagesEntity> findById(Long id);

    Page<DebugMessagesEntity> findAllPageable(Integer offset, Integer limit);

    Optional<DebugMessagesEntity> findByRequest(RequestEntity requestEntity);
}
