package ru.itmo.zavar.highloadproject.zorthtranslator.service;

import org.springframework.dao.DataAccessException;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.zorthtranslator.entity.zorth.RequestEntity;

import java.util.NoSuchElementException;

public interface ZorthTranslatorService {
    RequestEntity compileAndLinkage(boolean debug, String text, String username)
            throws ZorthException, NoSuchElementException, DataAccessException, ResponseStatusException;
}
