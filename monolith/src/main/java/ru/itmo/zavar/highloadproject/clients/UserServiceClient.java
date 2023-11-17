package ru.itmo.zavar.highloadproject.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.zavar.highloadproject.dto.inner.request.UserServiceRequest;
import ru.itmo.zavar.highloadproject.dto.inner.response.UserServiceResponse;

@FeignClient(name = "user")
public interface UserServiceClient {
    @PostMapping("/api/v1/user/save")
    ResponseEntity<UserServiceResponse> save(@RequestBody UserServiceRequest request); // TODO: обработать ошибки у всех вызовов

    @GetMapping("/api/v1/user/getById/{id}")
    ResponseEntity<UserServiceResponse> getById(@PathVariable Long id);

    @GetMapping("/api/v1/user/getByUsername/{username}")
    ResponseEntity<UserServiceResponse> getByUsername(@PathVariable String username);
}
