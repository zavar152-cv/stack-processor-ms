package ru.itmo.zavar.highloadprojectgateway.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Slf4j
@Component
public class GatewayErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        if (errorAttributes.get("status").equals(HttpStatus.NOT_FOUND.value())) {
            errorAttributes.put("message", "Route not found");
        }
        return errorAttributes;
    }
}
