package ru.itmo.zavar.highload.authservice.error;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.*;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {
    @Value("${spring.application.name}")
    private String name;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(webRequest, options);
        if (getError(webRequest) instanceof MethodArgumentNotValidException e) {
            /* Добавляем адекватный вывод ошибок валидации */
            Map<String, List<String>> errors = new HashMap<>();
            e.getFieldErrors().forEach(fieldError -> {
                String fieldName = fieldError.getField();
                String defaultMessage = fieldError.getDefaultMessage();
                errors.put(fieldName, Arrays.stream(Objects.requireNonNull(defaultMessage).split(","))
                        .map(StringUtils::capitalize)
                        .map(s -> s.endsWith(".") ? s : s + ".")
                        .toList());
            });
            errorAttributes.put("message", "Error during validation of request body");
            errorAttributes.put("errors", errors);
        } else {
            /* Убираем "/api/v1" из сообщений */
            String message = errorAttributes.get("message").toString().replace(contextPath, "");
            errorAttributes.put("message", message);
        }

        /* Убираем "/api/v1" из пути */
        String path = errorAttributes.get("path").toString().replace(contextPath, "/" + name);
        errorAttributes.put("path", path);

        return errorAttributes;
    }
}
