package ru.itmo.zavar.highloadproject.controller;

import com.google.gson.Gson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.exception.ControlUnitException;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highloadproject.dto.request.*;
import ru.itmo.zavar.highloadproject.dto.response.CompilerOutResponse;
import ru.itmo.zavar.highloadproject.dto.response.DebugMessagesResponse;
import ru.itmo.zavar.highloadproject.dto.response.ProcessorOutResponse;
import ru.itmo.zavar.highloadproject.dto.response.RequestResponse;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.service.ZorthProcessorService;
import ru.itmo.zavar.highloadproject.service.ZorthTranslatorService;
import ru.itmo.zavar.highloadproject.util.ZorthUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/zorth")
@RequiredArgsConstructor
public class ZorthController {

    private final ZorthTranslatorService zorthTranslatorService;
    private final ZorthProcessorService zorthProcessorService;

    @PostMapping("/pipeline")
    public ResponseEntity<?> pipeline(@Valid @RequestBody PipelineRequest pipelineRequest, Authentication authentication) {
        try {
            RequestEntity requestEntity = zorthTranslatorService.compileAndLinkage(pipelineRequest.debug(), pipelineRequest.text(), (UserEntity) authentication.getPrincipal());
            Optional<CompilerOutEntity> optionalCompilerOut = zorthTranslatorService.getCompilerOutputByRequestId(requestEntity.getId());
            if(optionalCompilerOut.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compiler out found");
            } else {
                CompilerOutEntity compilerOut = optionalCompilerOut.get();
                zorthProcessorService.startProcessorAndGetLogs(pipelineRequest.input(), compilerOut);
            }
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException | ZorthException | IllegalArgumentException
                 | ControlUnitException | ParseException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/compile")
    public ResponseEntity<?> compile(@Valid @RequestBody CompileRequest compileRequest, Authentication authentication) {
        try {
            zorthTranslatorService.compileAndLinkage(compileRequest.debug(), compileRequest.text(), (UserEntity) authentication.getPrincipal());
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException | ZorthException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @PostMapping("/execute")
    public ResponseEntity<?> execute(@Valid @RequestBody ExecuteRequest executeRequest, Authentication authentication) {
        try {
            if(!zorthTranslatorService.checkRequestOwnedByUser((UserEntity) authentication.getPrincipal(), executeRequest.requestId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't access this request");
            }
            Optional<CompilerOutEntity> optionalCompilerOut = zorthTranslatorService.getCompilerOutputByRequestId(executeRequest.requestId());
            if(optionalCompilerOut.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compiler out found");
            } else {
                CompilerOutEntity compilerOut = optionalCompilerOut.get();
                zorthProcessorService.startProcessorAndGetLogs(executeRequest.input(), compilerOut);
            }
            return ResponseEntity.ok().build();
        } catch (ControlUnitException | NoSuchElementException | ParseException exception) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
        }
    }

    @GetMapping("/getAllProcessorOut")
    public ResponseEntity<ArrayList<ProcessorOutResponse>> getAllProcessorOut(@Valid @RequestBody GetProcessorOutRequest processorOutRequest, Authentication authentication) {
        if(!zorthTranslatorService.checkRequestOwnedByUser((UserEntity) authentication.getPrincipal(), processorOutRequest.requestId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can't access this request");
        }
        ArrayList<ProcessorOutResponse> processorOutResponses = new ArrayList<>();
        zorthProcessorService.getAllProcessorOutByRequest(processorOutRequest.requestId()).forEach(processorOut -> {
            processorOutResponses.add(new ProcessorOutResponse(processorOut.getId(), processorOut.getInput(), processorOut.getTickLogs().split("\n")));
        });
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Requests-Count", String.valueOf(processorOutResponses.size()));
        return ResponseEntity.ok().headers(responseHeaders).body(processorOutResponses);
    }

    @GetMapping("/getDebugMessages/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<DebugMessagesResponse> getDebugMessages(@PathVariable Long id) {
        Optional<DebugMessagesEntity> optionalDebugMessages = zorthTranslatorService.getDebugMessages(id);
        if (optionalDebugMessages.isPresent()) {
            return ResponseEntity.ok(new DebugMessagesResponse(optionalDebugMessages.get().getId(), optionalDebugMessages.get().getText().split("\n")));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Debug messages not found");
        }
    }

    @GetMapping("/getCompilerOut/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<CompilerOutResponse> getCompilerOut(@PathVariable Long id) {
        Optional<CompilerOutEntity> optionalCompilerOut = zorthTranslatorService.getCompilerOutput(id);
        if (optionalCompilerOut.isPresent()) {
            CompilerOutEntity compilerOutEntity = optionalCompilerOut.get();
            ArrayList<Long> program = new ArrayList<>();
            ArrayList<Long> data = new ArrayList<>();
            ZorthUtil.fromByteArrayToLongList(program, compilerOutEntity.getProgram());
            ZorthUtil.fromByteArrayToLongList(data, compilerOutEntity.getData());
            return ResponseEntity.ok(new CompilerOutResponse(compilerOutEntity.getId(), program, data));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compiler output not found");
        }
    }

    @GetMapping("/getAllCompilerOut")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<CompilerOutResponse>> getAllCompilerOut(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                                                       @RequestParam(value = "limit", defaultValue = "3") @Min(1) @Max(50) Integer limit) {
        ArrayList<CompilerOutResponse> list = new ArrayList<>();
        zorthTranslatorService.getAllCompilerOutput(offset, limit).forEach(compilerOutEntity -> {
            ArrayList<Long> program = new ArrayList<>();
            ArrayList<Long> data = new ArrayList<>();
            ZorthUtil.fromByteArrayToLongList(program, compilerOutEntity.getProgram());
            ZorthUtil.fromByteArrayToLongList(data, compilerOutEntity.getData());
            list.add(new CompilerOutResponse(compilerOutEntity.getId(), program, data));
        });
        Page<CompilerOutResponse> page = new PageImpl<>(list);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/getAllDebugMessages")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<DebugMessagesResponse>> getAllDebugMessages(@RequestParam(value = "offset", defaultValue = "0") @Min(0) Integer offset,
                                                                           @RequestParam(value = "limit", defaultValue = "3") @Min(1) @Max(50) Integer limit) {
        ArrayList<DebugMessagesResponse> list = new ArrayList<>();
        zorthTranslatorService.getAllDebugMessages(offset, limit).forEach(debugMessagesEntity -> {
            list.add(new DebugMessagesResponse(debugMessagesEntity.getId(), debugMessagesEntity.getText().split("\n")));
        });

        Page<DebugMessagesResponse> page = new PageImpl<>(list);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/getAllRequests/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<ArrayList<RequestResponse>> getAllRequestsByUserId(@PathVariable Long id) {
        try {
            List<RequestEntity> allRequestsByUserId = zorthTranslatorService.getAllRequestsByUserId(id);
            ArrayList<RequestResponse> requestResponses = new ArrayList<>();
            allRequestsByUserId.forEach(requestEntity -> {
                requestResponses.add(new RequestResponse(requestEntity.getId(), requestEntity.getText().split("\n"), requestEntity.getDebug()));
            });
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Requests-Count", String.valueOf(requestResponses.size()));
            return ResponseEntity.ok().headers(responseHeaders).body(requestResponses);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/getRequests")
    @PreAuthorize("hasRole('ROLE_VIP')")
    public ResponseEntity<ArrayList<RequestResponse>> getRequestsForCurrentUser(Authentication authentication) {
        UserEntity principal = (UserEntity) authentication.getPrincipal();
        try {
            List<RequestEntity> allRequestsByUserId = zorthTranslatorService.getAllRequestsByUserId(principal.getId());
            ArrayList<RequestResponse> requestResponses = new ArrayList<>();
            allRequestsByUserId.forEach(requestEntity -> {
                requestResponses.add(new RequestResponse(requestEntity.getId(), requestEntity.getText().split("\n"), requestEntity.getDebug()));
            });
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.set("Requests-Count", String.valueOf(requestResponses.size()));
            return ResponseEntity.ok().headers(responseHeaders).body(requestResponses);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/getCompilerOutOfRequest")
    @PreAuthorize("hasRole('ROLE_VIP')")
    public ResponseEntity<CompilerOutResponse> getCompilerOutOfRequest(@Valid @RequestBody GetCompilerOutRequest request) {
        Optional<CompilerOutEntity> optionalCompilerOut = zorthTranslatorService.getCompilerOutputByRequestId(request.id());
        if (optionalCompilerOut.isPresent()) {
            CompilerOutEntity compilerOutEntity = optionalCompilerOut.get();
            ArrayList<Long> program = new ArrayList<>();
            ArrayList<Long> data = new ArrayList<>();
            ZorthUtil.fromByteArrayToLongList(program, compilerOutEntity.getProgram());
            ZorthUtil.fromByteArrayToLongList(data, compilerOutEntity.getData());
            return ResponseEntity.ok(new CompilerOutResponse(compilerOutEntity.getId(), program, data));
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Compiler output not found");
        }
    }

    @GetMapping("/getDebugMessagesOfRequest")
    @PreAuthorize("hasRole('ROLE_VIP')")
    public ResponseEntity<DebugMessagesResponse> getDebugMessagesOfRequest(@Valid @RequestBody GetDebugMessagesRequest request) {
        try {
            Optional<DebugMessagesEntity> optionalDebugMessages = zorthTranslatorService.getDebugMessagesByRequestId(request.id());
            if (optionalDebugMessages.isPresent()) {
                return ResponseEntity.ok(new DebugMessagesResponse(optionalDebugMessages.get().getId(), optionalDebugMessages.get().getText().split("\n")));
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Debug messages not found");
            }
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found");
        }
    }
}
