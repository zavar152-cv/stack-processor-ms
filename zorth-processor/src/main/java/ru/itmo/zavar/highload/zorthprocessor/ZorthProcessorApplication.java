package ru.itmo.zavar.highload.zorthprocessor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@EnableReactiveFeignClients
@SpringBootApplication(exclude = ReactiveUserDetailsServiceAutoConfiguration.class)
public class ZorthProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZorthProcessorApplication.class, args);
    }

}
