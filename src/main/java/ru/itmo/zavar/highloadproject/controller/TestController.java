package ru.itmo.zavar.highloadproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.zavar.highloadproject.repo.TestEntityRepository;

@RestController
public class TestController {

    @Autowired
    private TestEntityRepository testEntityRepository;

    @GetMapping("/getAllTestEntity")
    public ResponseEntity<?> getAllTestEntity() {
        System.out.println("a");
        return ResponseEntity.ok(testEntityRepository.findAll());
    }

}
