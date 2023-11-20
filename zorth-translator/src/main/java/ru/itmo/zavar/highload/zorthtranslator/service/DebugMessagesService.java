package ru.itmo.zavar.highload.zorthtranslator.service;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

import java.util.NoSuchElementException;

public interface DebugMessagesService {
    DebugMessagesEntity save(DebugMessagesEntity debugMessagesEntity) throws DataAccessException;

    DebugMessagesEntity findById(Long id) throws NoSuchElementException;

    DebugMessagesEntity findByRequestId(Long requestId) throws NoSuchElementException;

    Page<DebugMessagesEntity> findAllPageable(Integer offset, Integer limit);

    void deleteByRequest(RequestEntity requestEntity) throws DataAccessException;
}