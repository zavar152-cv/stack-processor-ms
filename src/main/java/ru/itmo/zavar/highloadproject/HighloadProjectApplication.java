package ru.itmo.zavar.highloadproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class HighloadProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HighloadProjectApplication.class, args);
    }

}
