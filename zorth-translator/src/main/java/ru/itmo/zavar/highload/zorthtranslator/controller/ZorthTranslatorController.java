package ru.itmo.zavar.highload.zorthtranslator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.request.CompileRequest;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.response.CompileResponse;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.response.GetCompilerOutResponse;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.response.GetDebugMessagesResponse;
import ru.itmo.zavar.highload.zorthtranslator.mapper.CompilerOutEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.mapper.DebugMessagesEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.mapper.RequestEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.service.CompilerOutService;
import ru.itmo.zavar.highload.zorthtranslator.service.DebugMessagesService;
import ru.itmo.zavar.highload.zorthtranslator.service.ZorthTranslatorService;
import ru.itmo.zavar.highload.zorthtranslator.util.RoleConstants;
import ru.itmo.zavar.highload.zorthtranslator.util.SpringWebFluxErrorModel;

import java.util.NoSuchElementException;

@Tag(name = "Zorth Translator Service Controller")
@RestController
@RequiredArgsConstructor
public class ZorthTranslatorController {
    private final ZorthTranslatorService zorthTranslatorService;
    private final CompilerOutService compilerOutService;
    private final DebugMessagesService debugMessagesService;

    private final DebugMessagesEntityMapper debugMessagesEntityMapper;
    private final CompilerOutEntityMapper compilerOutEntityMapper;
    private final RequestEntityMapper requestEntityMapper;

    @Operation(
            summary = "Compile",
            description = "This method compiles a request."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Request was successfully compiled",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetCompilerOutResponse.class)
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: request body isn't valid",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            )
    })
    @PostMapping("/compile")
    public Mono<CompileResponse> compile(@Valid @RequestBody CompileRequest compileRequest, Authentication authentication) {
        return zorthTranslatorService.compileAndLinkage(compileRequest.debug(), compileRequest.text(), authentication.getName())
                .map(requestEntityMapper::toDTO);
    }

    @Operation(
            summary = "Get compiler output of all requests / Get compiler output of request",
            description = """
                    If __only__ `offset` and `limit` parameters are present or there are __no parameters__, this method finds all compiler outputs.\\
                    \\
                    If __only__ `requestId` parameter is present, this method finds compiler output of a certain request.\\
                    \\
                    Can be called only by administrator.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compiler output(s) was(were) successfully obtained",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(oneOf = {Page.class, GetCompilerOutResponse.class}),
                            examples = {
                                    @ExampleObject(
                                            name = "Page<GetCompilerOutResponse>",
                                            description = "Page of compiler outputs"
                                    ),
                                    @ExampleObject(
                                            name = "GetCompilerOutResponse",
                                            description = "Compiler output for one request",
                                            value = """
                                                    {
                                                        "id": 1,
                                                        "requestId": 1,
                                                        "program": [721420291,989855748,637534208,989855748,671088640,0],
                                                        "data": [0,0,0,3,0]
                                                    }
                                                    """
                                    )
                            }
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: request parameters aren't valid",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden: only administrators can use this method",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            )
    })
    @GetMapping("/compiler-outs")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public Mono<Page<GetCompilerOutResponse>> getAllCompilerOut(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                                                @RequestParam(value = "limit", defaultValue = "3") @Min(1) @Max(50) Integer limit) {
        return compilerOutService.findAllPageable(offset, limit)
                .map(page -> page.map(compilerOutEntityMapper::toDTO));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compiler output(s) was(were) successfully obtained",
                    content = {@Content(mediaType = "application/json")}
            )
    })
    @GetMapping(value = "/compiler-outs", params = "request-id")
    @PreAuthorize("@zorthTranslatorServiceImpl.checkRequestOwnedByUser(authentication, #requestId)")
    public Mono<GetCompilerOutResponse> getCompilerOutOfRequest(@RequestParam(value = "request-id", required = false) Long requestId) {
        return compilerOutService.findByRequestId(requestId)
                .map(compilerOutEntityMapper::toDTO)
                .onErrorMap(NoSuchElementException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @Operation(
            summary = "Get compiler output by id",
            description = "This method finds compiler output by its id (if called by administrator)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compiler output was successfully obtained",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetCompilerOutResponse.class)
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: request parameter isn't valid",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden: only administrators can use this method",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "404", description = "Compiler output doesn't exist",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            )
    })
    @GetMapping("/compiler-outs/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public Mono<GetCompilerOutResponse> getCompilerOut(@PathVariable Long id) {
        return compilerOutService.findById(id)
                .map(compilerOutEntityMapper::toDTO)
                .onErrorMap(NoSuchElementException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @Operation(
            summary = "Get debug messages of all requests / Get debug messages of request",
            description = """
                    If __only__ `offset` and `limit` parameters are present or there are __no parameters__, this method finds all debug messages.\\
                    \\
                    If __only__ `requestId` parameter is present, this method finds debug messages of a certain request.\\
                    \\
                    Can be called only by administrator.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Debug messages were successfully obtained",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(oneOf = {Page.class, GetDebugMessagesResponse.class}),
                            examples = {
                                    @ExampleObject(
                                            name = "Page<GetDebugMessagesResponse>",
                                            description = "Page of debug messages"
                                    ),
                                    @ExampleObject(
                                            name = "GetDebugMessagesResponse",
                                            description = "Debug messages for one request",
                                            value = """
                                                    {
                                                        "id": 1,
                                                        "requestId": 1,
                                                        "text": ["","Tick: 1, TC: 1, Stage: FETCH, CR: 872415232 {NOPE}, IP: 0, AR: 0, TOS: 0, DS: null, RS: null, OUT: null, IN: k"]
                                                    }
                                                    """
                                    )
                            }
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: request parameters aren't valid",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden: only administrators can use this method",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            )
    })
    @GetMapping("/debug-messages")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public Mono<Page<GetDebugMessagesResponse>> getAllDebugMessages(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                                                    @RequestParam(value = "limit", defaultValue = "3") @Min(1) @Max(50) Integer limit) {
        return debugMessagesService.findAllPageable(offset, limit)
                .map(page -> page.map(debugMessagesEntityMapper::toDTO));
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Debug messages were successfully obtained",
                    content = {@Content(mediaType = "application/json")}
            )
    })
    @GetMapping(value = "/debug-messages", params = "request-id")
    @PreAuthorize("@zorthTranslatorServiceImpl.checkRequestOwnedByUser(authentication, #requestId)")
    public Mono<GetDebugMessagesResponse> getDebugMessagesOfRequest(@RequestParam(value = "request-id", required = false) Long requestId) {
        return debugMessagesService.findByRequestId(requestId)
                .map(debugMessagesEntityMapper::toDTO)
                .onErrorMap(NoSuchElementException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @Operation(
            summary = "Get debug messages by id",
            description = "This method finds debug messages by their ids (if called by administrator)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Debug messages were successfully obtained",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetDebugMessagesResponse.class)
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: path variable isn't valid",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden: only administrators can use this method",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "404", description = "Debug messages don't exist",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            )
    })
    @GetMapping("/debug-messages/{id}")
    @PreAuthorize("hasRole('" + RoleConstants.ADMIN + "')")
    public Mono<GetDebugMessagesResponse> getDebugMessages(@PathVariable Long id) {
        return debugMessagesService.findById(id)
                .map(debugMessagesEntityMapper::toDTO)
                .onErrorMap(NoSuchElementException.class, e -> new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage()));
    }
}
