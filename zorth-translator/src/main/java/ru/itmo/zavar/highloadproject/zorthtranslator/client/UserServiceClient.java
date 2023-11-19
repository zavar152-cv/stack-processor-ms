package ru.itmo.zavar.highloadproject.zorthtranslator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.itmo.zavar.highloadproject.zorthtranslator.config.feign.FeignConfiguration;
import ru.itmo.zavar.highloadproject.zorthtranslator.dto.inner.UserDTO;

@FeignClient(name = "user", path = "${server.servlet.context-path}/users", configuration = FeignConfiguration.class)
public interface UserServiceClient {
    @PutMapping
    ResponseEntity<?> saveUser(@RequestBody UserDTO dto);

    @GetMapping("/{username}")
    ResponseEntity<UserDTO> findUserByUsername(@PathVariable String username);
}
