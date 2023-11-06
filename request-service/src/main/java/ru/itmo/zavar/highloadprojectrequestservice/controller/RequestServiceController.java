package ru.itmo.zavar.highloadprojectrequestservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadprojectrequestservice.dto.inner.request.RequestServiceRequest;
import ru.itmo.zavar.highloadprojectrequestservice.dto.inner.response.RequestServiceResponse;
import ru.itmo.zavar.highloadprojectrequestservice.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadprojectrequestservice.mapper.RequestEntityMapper;
import ru.itmo.zavar.highloadprojectrequestservice.service.RequestService;

import java.util.Optional;

@RestController
@RequestMapping("/request")
@RequiredArgsConstructor
public class RequestServiceController {
    private final RequestService requestService;
    private final RequestEntityMapper mapper;

    @PostMapping("/save")
    public ResponseEntity<RequestServiceResponse> save(@Valid @RequestBody RequestServiceRequest request) {
        try {
            RequestEntity requestEntity = mapper.fromRequest(request);
            RequestServiceResponse response = mapper.toResponse(requestService.saveRequest(requestEntity));
            return ResponseEntity.ok(response);
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't save request");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@Valid @RequestBody RequestServiceRequest request) {
        try {
            RequestEntity requestEntity = mapper.fromRequest(request);
            requestService.deleteRequest(requestEntity);
            return ResponseEntity.ok().build();
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't delete request");
        }
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<RequestServiceResponse> get(@PathVariable Long id) {
        Optional<RequestEntity> requestEntity = requestService.findById(id);
        if (requestEntity.isPresent()) {
            RequestServiceResponse response = mapper.toResponse(requestEntity.get());
            return ResponseEntity.ok(response);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found");
        }
    }
}
