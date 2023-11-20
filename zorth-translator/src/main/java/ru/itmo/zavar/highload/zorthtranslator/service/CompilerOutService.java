package ru.itmo.zavar.highload.zorthtranslator.service;

import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

import java.util.NoSuchElementException;

public interface CompilerOutService {
    CompilerOutEntity save(CompilerOutEntity compilerOutEntity) throws DataAccessException;

    CompilerOutEntity findById(Long id) throws NoSuchElementException;

    CompilerOutEntity findByRequestId(Long requestId) throws NoSuchElementException;

    Page<CompilerOutEntity> findAllPageable(Integer offset, Integer limit);

    void deleteByRequest(RequestEntity requestEntity) throws DataAccessException;
}
