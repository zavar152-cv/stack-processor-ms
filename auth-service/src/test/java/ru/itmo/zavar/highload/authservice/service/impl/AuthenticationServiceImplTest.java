package ru.itmo.zavar.highload.authservice.service.impl;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highload.authservice.entity.security.UserEntity;
import ru.itmo.zavar.highload.authservice.service.JwtService;

import java.util.ArrayList;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest
@WireMockTest(httpPort = 7357)
class AuthenticationServiceImplTest {
    @MockBean
    private JwtService jwtService;

    @Autowired
    private AuthenticationServiceImpl authenticationService;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${test.id}")
    private Long id;

    @Value("${test.username}")
    private String username;

    @Value("${test.password}")
    private String password;

    @Value("${test.encrypted-password}")
    private String encryptedPassword;

    @Value("${test.jwt}")
    private String jwtToken;

    @Value("${response.user-found.filename}")
    private String responseUserFoundFilename;

    @Value("${response.user-not-found.filename}")
    private String responseUserNotFoundFilename;

    @Test
    public void signInAsExistingUser() {
        WireMock.stubFor(WireMock.get(contextPath + "/users/" + username).willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(responseUserFoundFilename)));
        when(jwtService.generateToken(Mockito.any())).thenReturn(jwtToken);
        assertEquals(jwtToken, authenticationService.signIn(username, password));
    }

    @Test
    public void signInAsNotExistingUser() {
        WireMock.stubFor(WireMock.get(contextPath + "/users/" + username).willReturn(WireMock.aResponse()
                .withStatus(404)
                .withHeader("Content-type", "application/json")
                .withBodyFile(responseUserNotFoundFilename)));
        assertThrows(InternalAuthenticationServiceException.class, () -> authenticationService.signIn(username, password));
    }

    @Test
    public void signInWithWrongPassword() {
        WireMock.stubFor(WireMock.get(contextPath + "/users/" + username).willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(responseUserFoundFilename)));
        when(jwtService.generateToken(Mockito.any())).thenReturn(jwtToken);
        assertThrows(BadCredentialsException.class, () -> authenticationService.signIn(username, ""));
    }

    @Test
    public void validateTokenForExistingUser() {
        WireMock.stubFor(WireMock.get(contextPath + "/users/" + username).willReturn(WireMock.aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(responseUserFoundFilename)));
        when(jwtService.extractUserName(jwtToken)).thenReturn(username);
        UserEntity userEntity = UserEntity.builder()
                .id(id)
                .username(username)
                .password(encryptedPassword)
                .roles(new HashSet<>())
                .requests(new ArrayList<>())
                .build();
        assertEquals(userEntity, authenticationService.validateToken(jwtToken));
    }

    @Test
    public void validateTokenForNotExistingUser() {
        WireMock.stubFor(WireMock.get(contextPath + "/users/" + username).willReturn(WireMock.aResponse()
                .withStatus(404)
                .withHeader("Content-type", "application/json")
                .withBodyFile(responseUserNotFoundFilename)));
        when(jwtService.extractUserName(jwtToken)).thenReturn(username);
        assertThrows(ResponseStatusException.class, () -> authenticationService.validateToken(jwtToken));
    }

    @Test
    public void validateExpiredToken() {
        when(jwtService.extractUserName(jwtToken)).thenThrow(ExpiredJwtException.class);
        assertThrows(ExpiredJwtException.class, () -> authenticationService.validateToken(jwtToken));
    }
}