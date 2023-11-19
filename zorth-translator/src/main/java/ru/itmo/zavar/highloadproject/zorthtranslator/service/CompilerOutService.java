package ru.itmo.zavar.highloadproject.zorthtranslator.service;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.RequestEntity;

import java.util.NoSuchElementException;

public interface CompilerOutService {
    CompilerOutEntity save(CompilerOutEntity compilerOutEntity) throws DataAccessException;

    CompilerOutEntity findById(Long id) throws NoSuchElementException;

    CompilerOutEntity findByRequestId(Long requestId) throws NoSuchElementException, ResponseStatusException;

    Page<CompilerOutEntity> findAllPageable(Integer offset, Integer limit);

    void deleteByRequest(RequestEntity requestEntity) throws DataAccessException;
}
