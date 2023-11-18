package ru.itmo.zavar.highloadproject.authservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.itmo.zavar.highloadproject.authservice.config.feign.FeignConfiguration;
import ru.itmo.zavar.highloadproject.authservice.dto.inner.UserDTO;

@FeignClient(name = "user", configuration = FeignConfiguration.class)
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{username}")
    ResponseEntity<UserDTO> findUserByUsername(@PathVariable String username);
}
