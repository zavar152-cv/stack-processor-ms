package ru.itmo.zavar.highload.authservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.itmo.zavar.highload.authservice.config.feign.FeignConfiguration;
import ru.itmo.zavar.highload.authservice.dto.inner.UserDTO;

@FeignClient(name = "user-service", path = "${server.servlet.context-path}", configuration = FeignConfiguration.class)
public interface UserServiceClient {
    @GetMapping("/users/{username}")
    @CircuitBreaker(name = "UserServiceClientCB")
    ResponseEntity<UserDTO> getUser(@PathVariable String username);
}
