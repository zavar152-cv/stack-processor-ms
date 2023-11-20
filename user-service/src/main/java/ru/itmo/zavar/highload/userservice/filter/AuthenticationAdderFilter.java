package ru.itmo.zavar.highload.userservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.apache.commons.lang.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

@Component
public class AuthenticationAdderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest,
                                    @NonNull HttpServletResponse httpServletResponse,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String username = httpServletRequest.getHeader("username");
        String authoritiesAsString = httpServletRequest.getHeader("authorities");

        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(authoritiesAsString)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }

        HashSet<SimpleGrantedAuthority> authorities = new HashSet<>(Arrays.stream(authoritiesAsString.split(","))
                .distinct().filter(StringUtils::isNotEmpty).map(SimpleGrantedAuthority::new).toList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

}