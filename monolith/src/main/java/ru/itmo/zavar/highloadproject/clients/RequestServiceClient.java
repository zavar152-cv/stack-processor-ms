package ru.itmo.zavar.highloadproject.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.zavar.highloadproject.dto.inner.request.RequestServiceRequest;
import ru.itmo.zavar.highloadproject.dto.inner.response.RequestServiceResponse;

@FeignClient(name = "request")
public interface RequestServiceClient {
    @PostMapping("/api/v1/request/save")
    ResponseEntity<RequestServiceResponse> save(@RequestBody RequestServiceRequest request);

    @DeleteMapping("/api/v1/request/delete")
    ResponseEntity<?> delete(@RequestBody RequestServiceRequest request);

    @GetMapping("/api/v1/request/get/{id}")
    ResponseEntity<RequestServiceResponse> get(@PathVariable Long id);
}
