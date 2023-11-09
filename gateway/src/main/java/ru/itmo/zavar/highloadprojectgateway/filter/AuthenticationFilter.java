package ru.itmo.zavar.highloadprojectgateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;
import ru.itmo.zavar.highloadprojectgateway.dto.inner.request.ValidateTokenRequest;
import ru.itmo.zavar.highloadprojectgateway.dto.inner.response.ValidateTokenResponse;

@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    public AuthenticationFilter(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        super(Config.class);
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("Incoming request to " + request.getPath());

            String authHeader = request.getHeaders().getFirst("Authorization");
            if (StringUtils.isEmpty(authHeader)) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authorization header provided"));
            }
            if (!StringUtils.startsWith(authHeader, "Bearer ")) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is invalid"));
            }
            String jwtToken = authHeader.substring(7);

            return webClientBuilder.build().post()
                    .uri("lb://auth/api/v1/validateToken")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ValidateTokenRequest.builder().jwtToken(jwtToken).build())
                    .retrieve()
                    .bodyToMono(ValidateTokenResponse.class)
                    .flatMap(response -> {
                        exchange.getRequest().mutate()
                                .header("username", response.username())
                                .header("authorities", response.authorities().stream()
                                        .reduce("", (a, b) -> a + "," + b))
                                .build();
                        return chain.filter(exchange);
                    }).onErrorResume(error -> {
                        if (error instanceof WebClientResponseException e) {
                            try {
                                JsonNode message = objectMapper.readTree(e.getResponseBodyAsString()).get("message");
                                return Mono.error(new ResponseStatusException(e.getStatusCode(), message.asText()));
                            } catch (JsonProcessingException ex) {
                                return Mono.error(new ResponseStatusException(e.getStatusCode()));
                            }
                        }
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Something went wrong during internal redirect"));
                    });
        });
    }

    public static class Config {
    }

}
