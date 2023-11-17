package ru.itmo.zavar.highloadproject.service;

import org.springframework.data.domain.Page;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;

import java.util.Optional;

public interface CompilerOutService {
    void deleteCompilerOutByRequest(RequestEntity requestEntity);

    CompilerOutEntity saveCompilerOut(CompilerOutEntity compilerOutEntity);

    Optional<CompilerOutEntity> findById(Long id);

    Page<CompilerOutEntity> findAllPageable(Integer offset, Integer limit);

    Optional<CompilerOutEntity> findByRequest(RequestEntity requestEntity);
}
