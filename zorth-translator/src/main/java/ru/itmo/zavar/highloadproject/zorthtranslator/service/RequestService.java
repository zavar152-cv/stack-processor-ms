package ru.itmo.zavar.highloadproject.zorthtranslator.service;

import org.springframework.dao.DataAccessException;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.RequestEntity;

import java.util.NoSuchElementException;

public interface RequestService {
    RequestEntity save(RequestEntity requestEntity) throws DataAccessException;

    RequestEntity findById(Long id) throws NoSuchElementException;

    void delete(RequestEntity requestEntity) throws DataAccessException;
}
