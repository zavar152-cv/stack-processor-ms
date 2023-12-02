package ru.itmo.zavar.highload.zorthtranslator;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.delegate.DatabaseDelegate;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;
import ru.itmo.zavar.highload.zorthtranslator.client.UserServiceClient;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.request.CompileRequest;
import ru.itmo.zavar.highload.zorthtranslator.dto.outer.response.GetCompilerOutResponse;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highload.zorthtranslator.entity.zorth.RequestEntity;
import ru.itmo.zavar.highload.zorthtranslator.mapper.CompilerOutEntityMapper;
import ru.itmo.zavar.highload.zorthtranslator.repo.CompilerOutRepository;
import ru.itmo.zavar.highload.zorthtranslator.repo.DebugMessagesRepository;
import ru.itmo.zavar.highload.zorthtranslator.repo.RequestRepository;
import ru.itmo.zavar.highload.zorthtranslator.util.RoleConstants;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 7357)
public class TranslatorTests {
    @LocalServerPort
    private Integer port;

    @Value("${spring.webflux.base-path}")
    private String contextPath;

    @Value("${user.username}")
    private String userUsername;

    @Value("${user.authorities}")
    private String userAuthorities;

    @Value("${user.request-id}")
    private Long userRequestId;

    @Value("${vip.username}")
    private String vipUsername;

    @Value("${vip.authorities}")
    private String vipAuthorities;

    @Value("${vip.request-id}")
    private Long vipRequestId;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.authorities}")
    private String adminAuthorities;

    @Value("${admin.request-id}")
    private Long adminRequestId;

    @Value("${response.get-user.filename1}")
    private String getUserResponseFilename1;

    @Value("${response.get-user.filename2}")
    private String getUserResponseFilename2;

    @Value("${response.get-user.filename3}")
    private String getUserResponseFilename3;

    @Value("${response.get-role.filename}")
    private String getRoleResponseFilename;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private CompilerOutRepository compilerOutRepository;

    @Autowired
    private DebugMessagesRepository debugMessagesRepository;

    @Autowired
    private CompilerOutEntityMapper compilerOutEntityMapper;

    @Autowired
    private UserServiceClient userServiceClient;

    private final RequestEntity requestEntity = new RequestEntity(null, "variable a\n3 a !\na @", true);

    private final CompilerOutEntity compilerOutEntity = CompilerOutEntity.builder()
            .id(null)
            .request(requestEntity)
            .data(new byte[]{1, 2, 3, 4, 5, 6, 7, 8})
            .program(new byte[]{8, 7, 6, 5, 4, 3, 2, 1})
            .build();

    private final DebugMessagesEntity debugMessagesEntity = new DebugMessagesEntity(null, requestEntity, "TEST");

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

    private static final DatabaseDelegate delegate = new JdbcDatabaseDelegate(postgres, "");

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port + contextPath;
        RestAssured.defaultParser = Parser.JSON;

        /* Чистим бд, чтобы ID соответствовали необходимым */
        ScriptUtils.runInitScript(delegate, "sql/clean.sql");

        /* Сохраняем тестовые данные для трёх пользователей (admin, user, vip) */
        for (long i = 1; i <= 3; i++) {
            requestEntity.setId(i);
            compilerOutEntity.setId(i);
            debugMessagesEntity.setId(i);
            requestRepository.save(requestEntity);
            compilerOutRepository.save(compilerOutEntity);
            debugMessagesRepository.save(debugMessagesEntity);
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    public void compileUnauthorized() {
        Response response = given()
                .header("Content-type", "application/json")
                .body(new CompileRequest(true, "variable a\n3 a !\na @"))
                .when()
                .post("/compile")
                .then()
                .extract()
                .response();
        assertEquals(401, response.statusCode());
    }

    @Test
    public void compileWithBadRequestBody() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body("{}")
                .when()
                .post("/compile")
                .then()
                .extract()
                .response();
        assertEquals(400, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void compileAsAdmin() {
        stubFor(get(contextPath + "/users/" + adminUsername).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getUserResponseFilename1)));
        stubFor(get(contextPath + "/roles/" + RoleConstants.USER).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getRoleResponseFilename)));
        stubFor(put(contextPath + "/users").willReturn(aResponse()
                .withStatus(200)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .body(new CompileRequest(true, "variable a\n3 a !\na @"))
                .when()
                .post("/compile")
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> verify(putRequestedFor(urlEqualTo(contextPath + "/users"))
                        .withRequestBody(matchingJsonPath("$.requests[0].id", equalTo(adminRequestId.toString())))
                        .withRequestBody(matchingJsonPath("$.requests[1].id", equalTo("4"))))
        );
    }

    @Test
    public void compileAsUser() {
        stubFor(get(contextPath + "/users/" + userUsername).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getUserResponseFilename2)));
        stubFor(get(contextPath + "/roles/" + RoleConstants.USER).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getRoleResponseFilename)));
        stubFor(put(contextPath + "/users").willReturn(aResponse()
                .withStatus(200)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body(new CompileRequest(true, "variable a\n3 a !\na @"))
                .when()
                .post("/compile")
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertFalse(requestRepository.findById(userRequestId).isPresent()),
                () -> assertFalse(compilerOutRepository.findById(userRequestId).isPresent()),
                () -> assertFalse(debugMessagesRepository.findById(userRequestId).isPresent()),
                () -> verify(putRequestedFor(urlEqualTo(contextPath + "/users"))
                        .withRequestBody(matchingJsonPath("$.requests[0].id", equalTo("4")))
                        .withRequestBody(matchingJsonPath("$.requests[1]", absent())))
        );
    }

    @Test
    public void compileAsVip() {
        stubFor(get(contextPath + "/users/" + vipUsername).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getUserResponseFilename3)));
        stubFor(get(contextPath + "/roles/" + RoleConstants.USER).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getRoleResponseFilename)));
        stubFor(put(contextPath + "/users").willReturn(aResponse()
                .withStatus(200)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .body(new CompileRequest(true, "variable a\n3 a !\na @"))
                .when()
                .post("/compile")
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> verify(putRequestedFor(urlEqualTo(contextPath + "/users"))
                        .withRequestBody(matchingJsonPath("$.requests[0].id", equalTo(vipRequestId.toString())))
                        .withRequestBody(matchingJsonPath("$.requests[1].id", equalTo("4"))))
        );
    }

    @Test
    public void getCompilerOutsAsAdmin() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/compiler-outs")
                .then()
                .extract()
                .response();
        requestEntity.setId(adminRequestId);
        compilerOutEntity.setId(adminRequestId);
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(compilerOutEntityMapper.toDTO(compilerOutEntity),
                        response.body().jsonPath().getList("content", GetCompilerOutResponse.class).get(0))
        );
    }

    @Test
    public void getCompilerOutsAsUser() {
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/compiler-outs")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode());
    }

    @Test
    public void getCompilerOutsAsVip() {
        Response response = given()
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .when()
                .get("/compiler-outs")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode());
    }

    @Test
    public void getNotExistingCompilerOut() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/compiler-outs/" + 0)
                .then()
                .extract()
                .response();
        assertEquals(404, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getCompilerOutAsAdmin() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/compiler-outs/" + adminRequestId)
                .then()
                .extract()
                .response();
        requestEntity.setId(adminRequestId);
        compilerOutEntity.setId(adminRequestId);
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(compilerOutEntityMapper.toDTO(compilerOutEntity),
                        response.body().as(GetCompilerOutResponse.class))
        );
    }

    @Test
    public void getCompilerOutAsUser() {
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/compiler-outs/" + userRequestId)
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode());
    }

    @Test
    public void getCompilerOutAsVip() {
        Response response = given()
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .when()
                .get("/compiler-outs/" + vipRequestId)
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode());
    }

    @Test
    public void getCompilerOutOfNotExistingRequest() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/compiler-outs?request-id=" + 10)
                .then()
                .extract()
                .response();
        assertEquals(404, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getCompilerOutOfOwnRequestAsAdmin() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/compiler-outs?request-id=" + adminRequestId)
                .then()
                .extract()
                .response();
        requestEntity.setId(adminRequestId);
        compilerOutEntity.setId(adminRequestId);
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(compilerOutEntityMapper.toDTO(compilerOutEntity),
                        response.body().as(GetCompilerOutResponse.class))
        );
    }

    @Test
    public void getCompilerOutOfNotOwnRequestAsAdmin() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/compiler-outs?request-id=" + vipRequestId)
                .then()
                .extract()
                .response();
        requestEntity.setId(vipRequestId);
        compilerOutEntity.setId(vipRequestId);
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(compilerOutEntityMapper.toDTO(compilerOutEntity),
                        response.body().as(GetCompilerOutResponse.class))
        );
    }

    @Test
    public void getCompilerOutOfOwnRequestAsVip() {
        stubFor(get(contextPath + "/users/" + vipUsername).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getUserResponseFilename3)));
        Response response = given()
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .when()
                .get("/compiler-outs?request-id=" + vipRequestId)
                .then()
                .extract()
                .response();
        requestEntity.setId(vipRequestId);
        compilerOutEntity.setId(vipRequestId);
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(compilerOutEntityMapper.toDTO(compilerOutEntity),
                        response.body().as(GetCompilerOutResponse.class))
        );
    }

    @Test
    public void getCompilerOutOfNotOwnRequestAsVip() {
        stubFor(get(contextPath + "/users/" + vipUsername).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getUserResponseFilename3)));
        Response response = given()
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .when()
                .get("/compiler-outs?request-id=" + adminRequestId)
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getCompilerOutOfOwnRequestAsUser() {
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/compiler-outs?request-id=" + userRequestId)
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getCompilerOutOfNotOwnRequestAsUser() {
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/compiler-outs?request-id=" + adminRequestId)
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getDebugMessagesAsAdmin() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/debug-messages")
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(adminRequestId, response.body().jsonPath().getLong("content[0].id")),
                () -> assertEquals(adminRequestId, response.body().jsonPath().getLong("content[0].requestId")),
                () -> assertEquals(debugMessagesEntity.getText(), response.body().jsonPath().getString("content[0].text[0]"))
        );
    }

    @Test
    public void getDebugMessagesAsUser() {
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/debug-messages")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode());
    }

    @Test
    public void getDebugMessagesAsVip() {
        Response response = given()
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .when()
                .get("/debug-messages")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode());
    }

    @Test
    public void getNotExistingDebugMessage() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/debug-messages/" + 0)
                .then()
                .extract()
                .response();
        assertEquals(404, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getDebugMessageAsAdmin() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/debug-messages/" + adminRequestId)
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(adminRequestId, response.body().jsonPath().getLong("id")),
                () -> assertEquals(adminRequestId, response.body().jsonPath().getLong("requestId")),
                () -> assertEquals(debugMessagesEntity.getText(), response.body().jsonPath().getString("text[0]"))
        );
    }

    @Test
    public void getDebugMessageAsUser() {
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/debug-messages/" + userRequestId)
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode());
    }

    @Test
    public void getDebugMessageAsVip() {
        Response response = given()
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .when()
                .get("/debug-messages/" + vipRequestId)
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode());
    }

    @Test
    public void getDebugMessageOfNotExistingRequest() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/debug-messages?request-id=" + 10)
                .then()
                .extract()
                .response();
        assertEquals(404, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getDebugMessageOfOwnRequestAsAdmin() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/debug-messages?request-id=" + adminRequestId)
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(adminRequestId, response.body().jsonPath().getLong("id")),
                () -> assertEquals(adminRequestId, response.body().jsonPath().getLong("requestId")),
                () -> assertEquals(debugMessagesEntity.getText(), response.body().jsonPath().getString("text[0]"))
        );
    }

    @Test
    public void getDebugMessageOfNotOwnRequestAsAdmin() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/debug-messages?request-id=" + vipRequestId)
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(vipRequestId, response.body().jsonPath().getLong("id")),
                () -> assertEquals(vipRequestId, response.body().jsonPath().getLong("requestId")),
                () -> assertEquals(debugMessagesEntity.getText(), response.body().jsonPath().getString("text[0]"))
        );
    }

    @Test
    public void getDebugMessageOfOwnRequestAsVip() {
        stubFor(get(contextPath + "/users/" + vipUsername).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getUserResponseFilename3)));
        Response response = given()
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .when()
                .get("/debug-messages?request-id=" + vipRequestId)
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(vipRequestId, response.body().jsonPath().getLong("id")),
                () -> assertEquals(vipRequestId, response.body().jsonPath().getLong("requestId")),
                () -> assertEquals(debugMessagesEntity.getText(), response.body().jsonPath().getString("text[0]"))
        );
    }

    @Test
    public void getDebugMessageOfNotOwnRequestAsVip() {
        stubFor(get(contextPath + "/users/" + vipUsername).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getUserResponseFilename3)));
        Response response = given()
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .when()
                .get("/debug-messages?request-id=" + adminRequestId)
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getDebugMessageOfOwnRequestAsUser() {
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/debug-messages?request-id=" + userRequestId)
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getDebugMessageOfNotOwnRequestAsUser() {
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/debug-messages?request-id=" + adminRequestId)
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
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
        stubFor(get(contextPath + "/users/" + adminUsername).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getUserResponseFilename1)));
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> userServiceClient.getUser(adminUsername).block());
        }
        WireMock.reset(); // сбрасываем для нового stubFor
        stubFor(get(contextPath + "/users/" + adminUsername).willReturn(aResponse().withStatus(503)));
        for (int i = 0; i < 3; i++) {
            if (i != 2) {
                assertThrows(ResponseStatusException.class, () -> userServiceClient.getUser(adminUsername).block());
            } else {
                assertThrows(CallNotPermittedException.class, () -> userServiceClient.getUser(adminUsername).block());
            }
        }

        /* Тестируем HALF-OPEN c возвратом в OPEN.
         * Он должен произойти тогда, когда оба вызова в состоянии HALF-OPEN не будут успешны. */
        System.out.println("Sleeping for 10 seconds...");
        Thread.sleep(10000); // спим 10с, чтобы осуществился переход из OPEN в HALF-OPEN
        for (int i = 0; i < 3; i++) {
            if (i != 2) {
                assertThrows(ResponseStatusException.class, () -> userServiceClient.getUser(adminUsername).block());
            } else {
                assertThrows(CallNotPermittedException.class, () -> userServiceClient.getUser(adminUsername).block());
            }
        }

        /* Тестируем HALF-OPEN c возвратом в CLOSED.
         * Он должен произойти тогда, когда оба вызова в состоянии HALF-OPEN будут успешны. */
        System.out.println("Sleeping for 10 seconds...");
        Thread.sleep(10000); // спим 10с, чтобы осуществился переход из OPEN в HALF-OPEN
        WireMock.reset(); // сбрасываем для нового stubFor
        stubFor(get(contextPath + "/users/" + adminUsername).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getUserResponseFilename1)));
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> userServiceClient.getUser(adminUsername).block());
        }

        /* Сбрасываем CB */
        cb.reset();
    }
}