package ru.itmo.zavar.highload.fileservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
import ru.itmo.zavar.highload.fileservice.util.SpringWebErrorModel;

import java.util.List;
import java.util.NoSuchElementException;

@Tag(name = "File Service Controller")
@RestController
@RequiredArgsConstructor
public class FileController {
    private final StorageService storageService;

    @Operation(
            summary = "Get all files",
            description = "This method finds information about all files (if called by administrator)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Information about files was successfully obtained",
                    content = {@Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = FileInfoResponse.class))
                    )}
            )
    })
    @GetMapping("/files")
    public ResponseEntity<List<FileInfoResponse>> listUploadedFiles(Authentication authentication) {
        return ResponseEntity.ok(storageService.listAll("LIST"));
    }

    @Operation(
            summary = "Get information about file",
            description = "This method finds information about file by its id (if file belongs to the caller)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Information about file was successfully obtained",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileInfoResponse.class)
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: invalid path variable",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden: file doesn't belong to the caller",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "404", description = "File doesn't exist",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            )
    })
    @GetMapping("/files/{id}/info")
    @PreAuthorize("@databaseStorageService.isOwnedByUser(authentication.name, #id)")
    public ResponseEntity<FileInfoResponse> getFileInfoById(@PathVariable @Positive @NotNull Long id) {
        try {
            FileInfoResponse fileInfoResponse = storageService.loadInfoById(id);
            return ResponseEntity.ok(fileInfoResponse);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(
            summary = "Get content of file",
            description = "This method finds content of file by its id (if file belongs to the caller)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Content of file was successfully obtained",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = FileContentResponse.class)
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: invalid path variable",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden: file doesn't belong to the caller",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "404", description = "File doesn't exist",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            )
    })
    @GetMapping("/files/{id}/content")
    @PreAuthorize("@databaseStorageService.isOwnedByUser(authentication.name, #id)")
    public ResponseEntity<FileContentResponse> getFileContentById(@PathVariable @Positive @NotNull Long id) {
        try {
            FileContentResponse fileContentResponse = storageService.loadContentById(id);
            return ResponseEntity.ok(fileContentResponse);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(
            summary = "Download file",
            description = "This method is for downloading file by its name (if file belongs to caller)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File was successfully obtained",
                    content = @Content(mediaType = "application/octet-stream")
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: invalid path variable",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden: file doesn't belong to the caller",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "404", description = "File doesn't exist",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            )
    })
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

    @Operation(
            summary = "Upload file",
            description = "This method uploads the file (if there is no conflict)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File was successfully uploaded", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request: invalid request parameter",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "409", description = "Conflict: file is empty or has already been uploaded",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            )
    })
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> handleFileUpload(@RequestParam("file") MultipartFile file, Authentication authentication) {
        try {
            storageService.store(file, authentication.getName());
            return ResponseEntity.ok().build();
        } catch (StorageException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }
}
