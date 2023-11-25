package ru.itmo.zavar.highload.zorthtranslator.service;

import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

public interface ZorthTranslatorService {
    Mono<RequestEntity> compileAndLinkage(boolean debug, String text, String username);

    Mono<Boolean> checkRequestOwnedByUser(Authentication authentication, Long requestId);
}
