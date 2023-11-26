package ru.itmo.zavar.highload.authservice.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highload.authservice.config.feign.FeignConfiguration;
import ru.itmo.zavar.highload.authservice.dto.inner.UserDTO;

@FeignClient(name = "user-service", path = "${server.servlet.context-path}", configuration = FeignConfiguration.class)
public interface UserServiceClient {
    @GetMapping("/users/{username}")
    @CircuitBreaker(name = "UserServiceClientCB", fallbackMethod = "fallback")
    ResponseEntity<UserDTO> getUser(@PathVariable String username);

    default ResponseEntity<UserDTO> fallback(String username, Exception e) {
        if (e instanceof ResponseStatusException ex && ex.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw ex;
        }
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Can't get user from user-service");
    }
}
