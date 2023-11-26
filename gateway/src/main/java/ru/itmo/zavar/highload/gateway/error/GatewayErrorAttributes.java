package ru.itmo.zavar.highload.gateway.error;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
public class GatewayErrorAttributes extends DefaultErrorAttributes {
    @Value("${context-path}")
    private String contextPath;

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        if (errorAttributes.get("status").equals(HttpStatus.NOT_FOUND.value())) {
            errorAttributes.put("message", "Route not found");
        } else {
            /* Убираем "/api/v1" из сообщений */
            String message = errorAttributes.get("message").toString().replace(contextPath, "");
            errorAttributes.put("message", message);
        }
        return errorAttributes;
    }
}
