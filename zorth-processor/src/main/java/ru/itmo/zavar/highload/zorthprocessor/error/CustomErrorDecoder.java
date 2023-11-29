package ru.itmo.zavar.highload.zorthprocessor.error;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class CustomErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String s, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        try (InputStream inputStream = response.body().asInputStream()) {
            JsonNode message = objectMapper.readTree(inputStream).get("message");
            return new ResponseStatusException(status, message.asText());
        } catch (IOException | NullPointerException e) {
            return new ResponseStatusException(status);
        }
    }
}
