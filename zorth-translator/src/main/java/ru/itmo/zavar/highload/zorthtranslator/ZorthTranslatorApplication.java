package ru.itmo.zavar.highload.zorthtranslator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import reactivefeign.spring.config.EnableReactiveFeignClients;

@EnableReactiveFeignClients
@SpringBootApplication(exclude = ReactiveUserDetailsServiceAutoConfiguration.class)
public class ZorthTranslatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZorthTranslatorApplication.class, args);
    }

}