package ru.itmo.zavar.highload.zorthtranslator.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highload.zorthtranslator.repo.CompilerOutRepository;
import ru.itmo.zavar.highload.zorthtranslator.service.CompilerOutService;
import ru.itmo.zavar.highload.zorthtranslator.service.RequestService;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.CompilerOutEntity;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CompilerOutServiceImpl implements CompilerOutService {
    private final CompilerOutRepository compilerOutRepository;
    private final RequestService requestService;

    @Override
    public CompilerOutEntity save(CompilerOutEntity compilerOutEntity) throws DataAccessException {
        return compilerOutRepository.save(compilerOutEntity);
    }

    @Override
    public CompilerOutEntity findById(Long id) throws NoSuchElementException {
        return compilerOutRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Compiler output not found"));
    }

    @Override
    public CompilerOutEntity findByRequestId(Long requestId) throws NoSuchElementException {
        RequestEntity requestEntity = requestService.findById(requestId);
        return compilerOutRepository.findByRequest(requestEntity).orElseThrow(() -> new NoSuchElementException("Compiler output not found"));
    }

    @Override
    public Page<CompilerOutEntity> findAllPageable(Integer offset, Integer limit) {
        return compilerOutRepository.findAll(PageRequest.of(offset, limit));
    }

    @Override
    public void deleteByRequest(RequestEntity requestEntity) throws DataAccessException {
        compilerOutRepository.deleteByRequest(requestEntity);
    }
}