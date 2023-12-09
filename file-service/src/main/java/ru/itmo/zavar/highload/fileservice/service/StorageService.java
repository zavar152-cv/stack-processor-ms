package ru.itmo.zavar.highload.fileservice.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.zavar.highload.fileservice.dto.response.FileContentResponse;
import ru.itmo.zavar.highload.fileservice.dto.response.FileInfoResponse;
import ru.itmo.zavar.highload.fileservice.exception.StorageException;

import java.util.List;
import java.util.NoSuchElementException;

public interface StorageService {
    void init();

    void store(MultipartFile file, String ownerName) throws StorageException;

    FileContentResponse loadContentByName(String filename) throws NoSuchElementException;

    FileContentResponse loadContentById(Long fileId) throws NoSuchElementException;

    FileInfoResponse loadInfoById(Long id) throws NoSuchElementException;

    FileInfoResponse loadInfoByName(String filename) throws NoSuchElementException;

    List<FileInfoResponse> listAll();

    Resource loadAsResource(String filename) throws NoSuchElementException;

    Resource loadAsResourceById(Long fileId) throws NoSuchElementException;

    void delete(String filename) throws NoSuchElementException;

    void deleteById(Long fileId) throws NoSuchElementException;

    void deleteAll();

    boolean isOwnedByUser(String username, String filename) throws NoSuchElementException;

    boolean isOwnedByUser(String username, Long id) throws NoSuchElementException;
}
