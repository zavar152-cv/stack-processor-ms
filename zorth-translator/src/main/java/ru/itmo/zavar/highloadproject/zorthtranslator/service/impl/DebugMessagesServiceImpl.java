package ru.itmo.zavar.highloadproject.zorthtranslator.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadproject.zorthtranslator.client.RequestServiceClient;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.mapper.RequestEntityMapper;
import ru.itmo.zavar.highloadproject.zorthtranslator.repo.DebugMessagesRepository;
import ru.itmo.zavar.highloadproject.zorthtranslator.service.DebugMessagesService;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class DebugMessagesServiceImpl implements DebugMessagesService {
    private final DebugMessagesRepository debugMessagesRepository;
    private final RequestServiceClient requestServiceClient;
    private final RequestEntityMapper requestEntityMapper;

    @Override
    public DebugMessagesEntity save(DebugMessagesEntity debugMessagesEntity) throws DataAccessException {
        return debugMessagesRepository.save(debugMessagesEntity);
    }

    @Override
    public DebugMessagesEntity findById(Long id) throws NoSuchElementException {
        return debugMessagesRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Debug messages not found"));
    }

    @Override
    public DebugMessagesEntity findByRequestId(Long requestId) throws NoSuchElementException {
        RequestEntity requestEntity = requestEntityMapper.fromDTO(requestServiceClient.get(requestId).getBody());
        return debugMessagesRepository.findByRequest(requestEntity).orElseThrow(() -> new NoSuchElementException("Debug messages not found"));
    }

    @Override
    public Page<DebugMessagesEntity> findAllPageable(Integer offset, Integer limit) {
        return debugMessagesRepository.findAll(PageRequest.of(offset, limit));
    }

    @Override
    public void deleteByRequest(RequestEntity requestEntity) throws DataAccessException {
        debugMessagesRepository.deleteByRequest(requestEntity);
    }
}
