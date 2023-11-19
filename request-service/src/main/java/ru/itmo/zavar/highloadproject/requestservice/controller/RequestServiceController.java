package ru.itmo.zavar.highloadproject.requestservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highloadproject.requestservice.dto.inner.RequestDTO;
import ru.itmo.zavar.highloadproject.requestservice.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.requestservice.mapper.RequestEntityMapper;
import ru.itmo.zavar.highloadproject.requestservice.service.RequestService;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestServiceController {
    private final RequestService requestService;
    private final RequestEntityMapper mapper;

    @PutMapping
    public ResponseEntity<RequestDTO> save(@Valid @RequestBody RequestDTO dto) {
        try {
            RequestEntity requestEntity = mapper.fromDTO(dto);
            dto = mapper.toDTO(requestService.save(requestEntity));
            return ResponseEntity.ok(dto);
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't save request");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequestDTO> get(@PathVariable Long id) {
        try {
            RequestEntity entity = requestService.findById(id);
            return ResponseEntity.ok(mapper.toDTO(entity));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            RequestEntity entity = requestService.findById(id);
            requestService.delete(entity);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (DataAccessException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Couldn't delete request");
        }
    }
}