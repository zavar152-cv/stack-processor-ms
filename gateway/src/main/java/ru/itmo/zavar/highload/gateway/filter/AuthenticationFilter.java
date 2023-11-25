package ru.itmo.zavar.highload.gateway.filter;

import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.itmo.zavar.highload.gateway.client.AuthServiceClient;
import ru.itmo.zavar.highload.gateway.dto.inner.request.ValidateTokenRequest;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    private final AuthServiceClient authServiceClient;

    public AuthenticationFilter(@Lazy AuthServiceClient authServiceClient) {
        super(Config.class);
        this.authServiceClient = authServiceClient;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
            if (StringUtils.isEmpty(authHeader)) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authorization header provided"));
            }
            if (!StringUtils.startsWith(authHeader, "Bearer ")) {
                return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization header is invalid"));
            }
            String jwtToken = authHeader.substring(7);

            // TODO: delete onErrorMap if circuit breaker can handle it
            return authServiceClient.validateToken(new ValidateTokenRequest(jwtToken))
                    .onErrorMap(IllegalArgumentException.class, e -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to find instance for auth"))
                    .flatMap(response -> {
                        exchange.getRequest().mutate()
                                .header("username", response.username())
                                .header("authorities", response.authorities().stream()
                                        .reduce("", (a, b) -> a + "," + b))
                                .build();
                        return chain.filter(exchange);
                    });
        });
    }

    public static class Config {
    }

}
