package ru.itmo.zavar.highload.zorthtranslator.filter;

import org.apache.commons.lang.StringUtils;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashSet;

@Component
public class AuthenticationAdderFilter implements WebFilter {
    @NonNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String username = request.getHeaders().getFirst("username");
        String authoritiesAsString = request.getHeaders().getFirst("authorities");

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(authoritiesAsString)) {
            return chain.filter(exchange);
        }

        HashSet<SimpleGrantedAuthority> authorities = new HashSet<>(Arrays.stream(authoritiesAsString.split(","))
                .distinct().filter(StringUtils::isNotEmpty).map(SimpleGrantedAuthority::new).toList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

        return chain.filter(exchange).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}