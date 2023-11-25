package ru.itmo.zavar.highload.zorthtranslator.client;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactivefeign.spring.config.ReactiveFeignClient;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthtranslator.config.feign.FeignConfiguration;
import ru.itmo.zavar.highload.zorthtranslator.dto.inner.RoleDTO;
import ru.itmo.zavar.highload.zorthtranslator.dto.inner.UserDTO;

@Component
@ReactiveFeignClient(name = "user-service", path = "${spring.webflux.base-path}", configuration = FeignConfiguration.class)
public interface UserServiceClient {
    @PutMapping("/users")
    Mono<Void> saveUser(@RequestBody UserDTO dto);

    @GetMapping("/users/{username}")
    Mono<UserDTO> getUser(@PathVariable String username);

    @GetMapping("/roles/{name}")
    Mono<RoleDTO> getRole(@PathVariable String name);
}
