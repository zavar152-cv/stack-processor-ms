package ru.itmo.zavar.highload.zorthtranslator.service;

import org.springframework.dao.DataAccessException;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;

import java.util.NoSuchElementException;

public interface ZorthTranslatorService {
    RequestEntity compileAndLinkage(boolean debug, String text, String username)
            throws ZorthException, NoSuchElementException, DataAccessException, ResponseStatusException;

    boolean checkRequestOwnedByUser(String username, Long requestId) throws ResponseStatusException;
}
