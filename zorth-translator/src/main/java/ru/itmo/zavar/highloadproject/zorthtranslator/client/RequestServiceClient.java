package ru.itmo.zavar.highloadproject.zorthtranslator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itmo.zavar.highloadproject.zorthtranslator.config.feign.FeignConfiguration;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.inner.RequestDTO;

@FeignClient(name = "request", path = "${server.servlet.context-path}/requests", configuration = FeignConfiguration.class)
public interface RequestServiceClient {
    @PutMapping
    ResponseEntity<RequestDTO> save(@RequestBody RequestDTO dto);

    @GetMapping("/{id}")
    ResponseEntity<RequestDTO> get(@PathVariable Long id);

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable Long id);
}
