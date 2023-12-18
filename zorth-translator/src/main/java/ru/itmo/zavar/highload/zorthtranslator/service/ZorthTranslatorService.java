package ru.itmo.zavar.highload.zorthtranslator.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

public interface ZorthTranslatorService {

    Mono<RequestEntity> compileAndLinkageFromFile(boolean debug, String username, Long fileId, String email) throws InterruptedException, EntityNotFoundException, IllegalArgumentException;

    Mono<RequestEntity> compileAndLinkage(boolean debug, String text, String username, String email);

    Mono<RequestEntity> compileAndLinkageFromFile(boolean debug, String username, Long fileId) throws InterruptedException, EntityNotFoundException, IllegalArgumentException;

    Mono<RequestEntity> compileAndLinkage(boolean debug, String text, String username);

    Mono<Boolean> checkRequestOwnedByUser(Authentication authentication, Long requestId);
}
