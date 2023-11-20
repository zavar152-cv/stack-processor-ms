package ru.itmo.zavar.highload.zorthtranslator.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highload.zorthtranslator.repo.RequestRepository;
import ru.itmo.zavar.highload.zorthtranslator.service.RequestService;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;

    @Override
    public RequestEntity save(RequestEntity requestEntity) throws DataAccessException {
        return requestRepository.save(requestEntity);
    }

    @Override
    public RequestEntity findById(Long id) throws NoSuchElementException {
        return requestRepository.findById(id).orElseThrow(() -> new NoSuchElementException("Request not found"));
    }

    @Override
    public void delete(RequestEntity requestEntity) throws DataAccessException {
        requestRepository.delete(requestEntity);
    }
}
