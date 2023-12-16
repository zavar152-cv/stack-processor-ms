package ru.itmo.zavar.highload.zorthprocessor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.ExecuteRequest;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.PipelineRequest;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.response.GetProcessorOutResponse;
import ru.itmo.zavar.highload.zorthprocessor.mapper.ProcessorOutEntityMapper;
import ru.itmo.zavar.highload.zorthprocessor.service.ZorthProcessorService;
import ru.itmo.zavar.highload.zorthprocessor.util.SpringWebFluxErrorModel;

@Tag(name = "Zorth Processor Service Controller")
@RestController
@RequiredArgsConstructor
public class ZorthProcessorController {
    private final ZorthProcessorService zorthProcessorService;
    private final ProcessorOutEntityMapper processorOutEntityMapper;

    @Operation(
            summary = "Pipeline",
            description = "This method runs pipeline - it compiles request and executes it right away."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pipeline was successfully run",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetProcessorOutResponse.class)
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: request body isn't valid",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            )
    })
    @PostMapping("/pipeline")
    public Mono<GetProcessorOutResponse> pipeline(@Valid @RequestBody PipelineRequest request, Authentication authentication) {
        return zorthProcessorService.pipeline(request.debug(), request.text(), request.input(), authentication)
                .map(processorOutEntityMapper::toDTO);
    }

    @Operation(
            summary = "Execute",
            description = "This method executes compiled request."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Compiled request was successfully executed",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetProcessorOutResponse.class)
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: request body isn't valid",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden: non-admin users can execute only own requests",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "404", description = "Request doesn't exist",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            )
    })
    @PostMapping("/execute")
    public Mono<GetProcessorOutResponse> execute(@Valid @RequestBody ExecuteRequest request, Authentication authentication) {
        return zorthProcessorService.startProcessorAndGetLogs(request.input(), request.requestId(), authentication)
                .map(processorOutEntityMapper::toDTO);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Processor output of request was successfully obtained",
                    content = {@Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = GetProcessorOutResponse.class))
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: request body isn't valid",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden: non-admin users can get processor output only of own requests",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "404", description = "Request doesn't exist",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebFluxErrorModel.class)
                    )}
            )
    })
    @GetMapping(value = "/processor-outs", params = "request-id")
    public Flux<GetProcessorOutResponse> getProcessorOutOfRequest(@RequestParam("request-id") Long requestId, Authentication authentication) {
        return zorthProcessorService.findAllProcessorOutByRequestId(requestId, authentication)
                .map(processorOutEntityMapper::toDTO);
    }
}
