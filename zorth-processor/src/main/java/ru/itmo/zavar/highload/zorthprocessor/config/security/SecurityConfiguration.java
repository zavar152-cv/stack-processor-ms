package ru.itmo.zavar.highload.zorthprocessor.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import ru.itmo.zavar.highload.zorthprocessor.filter.AuthenticationAdderFilter;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final AuthenticationAdderFilter filter;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/pipeline", "/execute", "/processor-outs/**").authenticated()
                        .anyExchange().permitAll())
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance()) // STATELESS
                .addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION);
        return http.build();
    }
}
