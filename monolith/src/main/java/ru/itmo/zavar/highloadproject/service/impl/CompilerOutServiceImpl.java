package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.repo.CompilerOutRepository;
import ru.itmo.zavar.highloadproject.service.CompilerOutService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompilerOutServiceImpl implements CompilerOutService {
    private final CompilerOutRepository compilerOutRepository;

    @Override
    public void deleteCompilerOutByRequest(RequestEntity requestEntity) {
        compilerOutRepository.deleteByRequest(requestEntity);
    }

    @Override
    public CompilerOutEntity saveCompilerOut(CompilerOutEntity compilerOutEntity) {
        return compilerOutRepository.save(compilerOutEntity);
    }

    @Override
    public Optional<CompilerOutEntity> findById(Long id) {
        return compilerOutRepository.findById(id);
    }

    @Override
    public Page<CompilerOutEntity> findAllPageable(Integer offset, Integer limit) {
        return compilerOutRepository.findAll(PageRequest.of(offset, limit));
    }

    @Override
    public Optional<CompilerOutEntity> findByRequest(RequestEntity requestEntity) {
        return compilerOutRepository.findByRequest(requestEntity);
    }
}
