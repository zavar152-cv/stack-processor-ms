package ru.itmo.zavar.highloadprojectrequestservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadprojectrequestservice.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadprojectrequestservice.repo.RequestRepository;
import ru.itmo.zavar.highloadprojectrequestservice.service.RequestService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;

    @Override
    public RequestEntity saveRequest(RequestEntity requestEntity) {
        return requestRepository.save(requestEntity);
    }

    @Override
    public void deleteRequest(RequestEntity requestEntity) {
        requestRepository.delete(requestEntity);
    }

    @Override
    public Optional<RequestEntity> findById(Long id) {
        return requestRepository.findById(id);
    }
}
