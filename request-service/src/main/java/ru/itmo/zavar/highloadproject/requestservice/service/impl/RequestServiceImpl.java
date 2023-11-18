package ru.itmo.zavar.highloadproject.requestservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadproject.requestservice.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.requestservice.repo.RequestRepository;
import ru.itmo.zavar.highloadproject.requestservice.service.RequestService;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;

    @Override
    public RequestEntity save(RequestEntity requestEntity) throws DataAccessException {
        return requestRepository.save(requestEntity);
    }

    @Override
    public RequestEntity findById(Long id) throws IllegalArgumentException {
        return requestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
    }

    @Override
    public void delete(RequestEntity requestEntity) throws DataAccessException {
        requestRepository.delete(requestEntity);
    }
}
