package ru.itmo.zavar.highloadproject.zorthtranslator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class ZorthTranslatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZorthTranslatorApplication.class, args);
    }

}