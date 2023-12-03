package ru.itmo.zavar.highload.authservice.service.impl;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highload.authservice.client.UserServiceClient;
import ru.itmo.zavar.highload.authservice.entity.security.UserEntity;
import ru.itmo.zavar.highload.authservice.service.JwtService;

import java.util.ArrayList;
import java.util.HashSet;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@WireMockTest
class AuthTests {
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

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private UserServiceClient userServiceClient;

    private static WireMockRuntimeInfo wireMockRuntimeInfo;

    @BeforeAll
    static void beforeAll(WireMockRuntimeInfo info) {
        wireMockRuntimeInfo = info;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("wiremock.url", wireMockRuntimeInfo::getHttpBaseUrl);
    }

    @Test
    public void signInAsExistingUser() {
        stubFor(get(contextPath + "/users/" + username).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(responseUserFoundFilename)));
        when(jwtService.generateToken(any())).thenReturn(jwtToken);
        assertEquals(jwtToken, authenticationService.signIn(username, password));
    }

    @Test
    public void signInAsNotExistingUser() {
        stubFor(get(contextPath + "/users/" + username).willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-type", "application/json")
                .withBodyFile(responseUserNotFoundFilename)));
        assertThrows(InternalAuthenticationServiceException.class, () -> authenticationService.signIn(username, password));
    }

    @Test
    public void signInWithWrongPassword() {
        stubFor(get(contextPath + "/users/" + username).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(responseUserFoundFilename)));
        when(jwtService.generateToken(any())).thenReturn(jwtToken);
        assertThrows(BadCredentialsException.class, () -> authenticationService.signIn(username, ""));
    }

    @Test
    public void validateTokenForExistingUser() {
        stubFor(get(contextPath + "/users/" + username).willReturn(aResponse()
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
        stubFor(get(contextPath + "/users/" + username).willReturn(aResponse()
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

    @Test
    public void testCircuitBreaker() throws InterruptedException {
        /* Включаем логирование событий на CB и сбрасываем его */
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("UserServiceClientCB");
        cb.reset();
        cb.getEventPublisher()
                .onError(System.out::println)
                .onSuccess(System.out::println)
                .onCallNotPermitted(System.out::println)
                .onStateTransition(System.out::println);

        /* Тестируем CLOSED с переходом в OPEN.
         * Переход должен произойти, если 2 вызова из 5 завершатся с ошибкой. */
        stubFor(get(contextPath + "/users/" + username).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(responseUserFoundFilename)));
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> userServiceClient.getUser(username));
        }
        WireMock.reset(); // сбрасываем для нового stubFor
        stubFor(get(contextPath + "/users/" + username).willReturn(aResponse().withStatus(503)));
        for (int i = 0; i < 3; i++) {
            if (i != 2) {
                assertThrows(ResponseStatusException.class, () -> userServiceClient.getUser(username));
            } else {
                assertThrows(CallNotPermittedException.class, () -> userServiceClient.getUser(username));
            }
        }

        /* Тестируем HALF-OPEN c возвратом в OPEN.
         * Он должен произойти тогда, когда оба вызова в состоянии HALF-OPEN не будут успешны. */
        System.out.println("Sleeping for 10 seconds...");
        Thread.sleep(10000); // спим 10с, чтобы осуществился переход из OPEN в HALF-OPEN
        for (int i = 0; i < 3; i++) {
            if (i != 2) {
                assertThrows(ResponseStatusException.class, () -> userServiceClient.getUser(username));
            } else {
                assertThrows(CallNotPermittedException.class, () -> userServiceClient.getUser(username));
            }
        }

        /* Тестируем HALF-OPEN c возвратом в CLOSED.
         * Он должен произойти тогда, когда оба вызова в состоянии HALF-OPEN будут успешны. */
        System.out.println("Sleeping for 10 seconds...");
        Thread.sleep(10000); // спим 10с, чтобы осуществился переход из OPEN в HALF-OPEN
        WireMock.reset(); // сбрасываем для нового stubFor
        stubFor(get(contextPath + "/users/" + username).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(responseUserFoundFilename)));
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> userServiceClient.getUser(username));
        }

        /* Сбрасываем CB */
        cb.reset();
    }
}