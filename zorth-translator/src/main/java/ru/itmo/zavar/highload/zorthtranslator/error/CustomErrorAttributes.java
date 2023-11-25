package ru.itmo.zavar.highload.zorthtranslator.error;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.*;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {
    @Value("${spring.application.name}")
    private String name;

    @Value("${spring.webflux.base-path}")
    private String contextPath;

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        if (getError(webRequest) instanceof WebExchangeBindException ex) {
            Map<String, List<String>> errors = new HashMap<>();
            ex.getFieldErrors().forEach(fieldError -> {
                String fieldName = fieldError.getField();
                String defaultMessage = fieldError.getDefaultMessage();
                errors.put(fieldName, Arrays.stream(Objects.requireNonNull(defaultMessage).split(","))
                        .map(StringUtils::capitalize)
                        .map(s -> s.endsWith(".") ? s : s + ".")
                        .toList());
            });
            errorAttributes.put("message", "Error during validation of request body");
            errorAttributes.put("errors", errors);
        }
        String path = errorAttributes.get("path").toString().replace(contextPath, "/" + name.split("-")[0]);
        errorAttributes.put("path", path);
        return errorAttributes;
    }
}
