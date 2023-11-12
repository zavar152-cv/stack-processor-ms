package ru.itmo.zavar.highloadproject.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.highloadproject.clients.UserServiceClient;
import ru.itmo.zavar.highloadproject.dto.inner.response.UserServiceResponse;
import ru.itmo.zavar.highloadproject.mapper.UserEntityMapper;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserServiceClient userServiceClient;
    private final UserEntityMapper userEntityMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ResponseEntity<UserServiceResponse> response = userServiceClient.getByUsername(username);
        if (response.getStatusCode().isError()) {
            throw new UsernameNotFoundException("User not found");
        }
        return userEntityMapper.fromResponse(response.getBody());
    }
}
