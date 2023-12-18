package ru.itmo.zavar.highload.fileservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.itmo.zavar.highload.fileservice.dto.response.FileContentResponse;
import ru.itmo.zavar.highload.fileservice.dto.response.FileInfoResponse;
import ru.itmo.zavar.highload.fileservice.entity.FileEntity;
import ru.itmo.zavar.highload.fileservice.exception.StorageException;
import ru.itmo.zavar.highload.fileservice.repo.FileRepository;
import ru.itmo.zavar.highload.fileservice.service.StorageService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Transactional
@Service
@RequiredArgsConstructor
public class DatabaseStorageService implements StorageService {
    private final FileRepository fileRepository;

    @Override
    public void init() {

    }

    @Override
    public void store(MultipartFile file, String ownerName) throws StorageException {
        if (file.isEmpty()) {
            throw new StorageException("Failed to store empty file");
        }
        try {
            String filename = file.getOriginalFilename();
            byte[] bytes = file.getBytes();

            FileEntity fileEntity = FileEntity.builder()
                    .name(filename)
                    .ownerName(ownerName)
                    .content(bytes).build();

            fileRepository.save(fileEntity);
        } catch (IOException e) {
            throw new StorageException("Failed to store file", e);
        }
    }

    @Override
    public FileContentResponse loadContentByName(String filename) throws NoSuchElementException {
        FileEntity fileEntity = fileRepository.findByName(filename).orElseThrow();
        byte[] content = fileEntity.getContent();
        return new FileContentResponse(fileEntity.getId(), fileEntity.getName(), new String(content));
    }

    @Override
    public FileContentResponse loadContentById(Long fileId) throws NoSuchElementException {
        FileEntity fileEntity = fileRepository.findById(fileId).orElseThrow();
        byte[] content = fileEntity.getContent();
        return new FileContentResponse(fileEntity.getId(), fileEntity.getName(), new String(content));
    }

    @Override
    public FileInfoResponse loadInfoById(Long id) throws NoSuchElementException {
        FileEntity fileEntity = fileRepository.findById(id).orElseThrow();
        return new FileInfoResponse(fileEntity.getId(),
                fileEntity.getName(), fileEntity.getOwnerName());
    }

    @Override
    public FileInfoResponse loadInfoByName(String filename) throws NoSuchElementException {
        FileEntity fileEntity = fileRepository.findByName(filename).orElseThrow();
        return new FileInfoResponse(fileEntity.getId(),
                fileEntity.getName(), fileEntity.getOwnerName());
    }

    @Override
    public List<FileInfoResponse> listAll() {
        Iterable<FileEntity> iterable = fileRepository.findAll();
        List<FileInfoResponse> all = new ArrayList<>();
        iterable.forEach(fileEntity -> {
            all.add(new FileInfoResponse(fileEntity.getId(),
                    fileEntity.getName(), fileEntity.getOwnerName()));
        });
        return all;
    }

    @Override
    public Resource loadAsResource(String filename) throws NoSuchElementException {
        return new ByteArrayResource(loadContentByName(filename).content().getBytes());
    }

    @Override
    public Resource loadAsResourceById(Long fileId) throws NoSuchElementException {
        return new ByteArrayResource(loadContentById(fileId).content().getBytes());
    }

    @Override
    public void delete(String filename) throws NoSuchElementException {
        fileRepository.findByName(filename).orElseThrow();
        fileRepository.deleteByName(filename);
    }

    @Override
    public void deleteById(Long fileId) throws NoSuchElementException {
        fileRepository.findById(fileId).orElseThrow();
        fileRepository.deleteById(fileId);
    }

    @Override
    public void deleteAll() {
        fileRepository.deleteAll();
    }

    @Override
    public boolean isOwnedByUser(String username, String filename) throws NoSuchElementException {
        FileEntity fileEntity = fileRepository.findByName(filename).orElseThrow();
        return fileEntity.getOwnerName().equals(username);
    }

    @Override
    public boolean isOwnedByUser(String username, Long id) throws NoSuchElementException {
        FileEntity fileEntity = fileRepository.findById(id).orElseThrow();
        return fileEntity.getOwnerName().equals(username);
    }
}
