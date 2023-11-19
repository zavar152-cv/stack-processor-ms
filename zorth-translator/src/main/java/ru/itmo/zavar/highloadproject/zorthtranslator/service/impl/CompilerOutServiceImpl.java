package ru.itmo.zavar.highloadproject.zorthtranslator.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadproject.zorthtranslator.client.RequestServiceClient;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.mapper.RequestEntityMapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.repo.CompilerOutRepository;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.CompilerOutService;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CompilerOutServiceImpl implements CompilerOutService {
    private final CompilerOutRepository compilerOutRepository;
    private final RequestServiceClient requestServiceClient;
    private final RequestEntityMapper requestEntityMapper;

    @Override
    public CompilerOutEntity save(CompilerOutEntity compilerOutEntity) throws DataAccessException {
        return compilerOutRepository.save(compilerOutEntity);
    }

    @Override
    public CompilerOutEntity findById(Long id) throws NoSuchElementException {
        return compilerOutRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Compiler output not found"));
    }

    @Override
    public CompilerOutEntity findByRequestId(Long requestId) throws NoSuchElementException, ResponseStatusException {
        RequestEntity requestEntity = requestEntityMapper.fromDTO(requestServiceClient.get(requestId).getBody());
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