package ru.itmo.zavar.highload.fileservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highload.fileservice.dto.response.FileContentResponse;
import ru.itmo.zavar.highload.fileservice.dto.response.FileInfoResponse;
import ru.itmo.zavar.highload.fileservice.exception.StorageException;
import ru.itmo.zavar.highload.fileservice.service.StorageService;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
public class FileUploadController {
    private final StorageService storageService;

    @GetMapping("/files")
    public ResponseEntity<List<FileInfoResponse>> listUploadedFiles() {
        return ResponseEntity.ok(storageService.listAll());
    }

    @GetMapping("/files/info/{id}")
    @PreAuthorize("@databaseStorageService.isOwnedByUser(authentication.name, #id)")
    public ResponseEntity<FileInfoResponse> getFileInfoById(@PathVariable Long id) {
        try {
            FileInfoResponse fileInfoResponse = storageService.loadInfoById(id);
            return ResponseEntity.ok(fileInfoResponse);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/files/content/{id}")
    @PreAuthorize("@databaseStorageService.isOwnedByUser(authentication.name, #id)")
    public ResponseEntity<FileContentResponse> getFileContentById(@PathVariable Long id) {
        try {
            FileContentResponse fileContentResponse = storageService.loadContentById(id);
            return ResponseEntity.ok(fileContentResponse);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }


    @GetMapping("/download/{filename:.+}")
    @ResponseBody
    @PreAuthorize("@databaseStorageService.isOwnedByUser(authentication.name, #filename)")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Resource file = storageService.loadAsResource(filename);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + filename + "\"").body(file);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            storageService.store(file, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
