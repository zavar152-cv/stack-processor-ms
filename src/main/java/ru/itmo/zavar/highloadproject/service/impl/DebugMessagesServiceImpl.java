package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.repo.DebugMessagesRepository;
import ru.itmo.zavar.highloadproject.service.DebugMessagesService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DebugMessagesServiceImpl implements DebugMessagesService {
    private final DebugMessagesRepository debugMessagesRepository;

    @Override
    public void deleteDebugMessagesByRequest(RequestEntity requestEntity) {
        debugMessagesRepository.deleteByRequest(requestEntity);
    }

    @Override
    public DebugMessagesEntity saveDebugMessages(DebugMessagesEntity debugMessagesEntity) {
        return debugMessagesRepository.save(debugMessagesEntity);
    }

    @Override
    public Optional<DebugMessagesEntity> findById(Long id) {
        return debugMessagesRepository.findById(id);
    }

    @Override
    public Page<DebugMessagesEntity> findAllPageable(Integer offset, Integer limit) {
        return debugMessagesRepository.findAll(PageRequest.of(offset, limit));
    }

    @Override
    public Optional<DebugMessagesEntity> findByRequest(RequestEntity requestEntity) {
        return debugMessagesRepository.findByRequest(requestEntity);
    }
}
