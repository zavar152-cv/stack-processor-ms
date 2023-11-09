package ru.itmo.zavar.highloadprojectauthservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.itmo.zavar.highloadprojectauthservice.dto.inner.response.UserEntityResponse;

@FeignClient(name = "user")
public interface UserServiceClient {
    @GetMapping("/api/v1/getByUsername/{username}")
    ResponseEntity<UserEntityResponse> getByUsername(@PathVariable String username);
}
