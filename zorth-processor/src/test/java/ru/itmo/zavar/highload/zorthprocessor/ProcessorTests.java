package ru.itmo.zavar.highload.zorthprocessor;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
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
import ru.itmo.zavar.highload.zorthprocessor.client.ZorthTranslatorClient;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.ExecuteRequest;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.PipelineRequest;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest
public class ProcessorTests {
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

    @Value("${response.get-compiler-out.filename1}")
    private String getCompilerOutResponseFilename1;

    @Value("${response.get-compiler-out.filename2}")
    private String getCompilerOutResponseFilename2;

    @Value("${response.get-compiler-out.filename3}")
    private String getCompilerOutResponseFilename3;

    @Value("${response.compiler-out-not-found.filename}")
    private String compilerOutNotFoundResponseFilename;

    @Value("${response.compile.filename}")
    private String compileResponseFilename;

    @Value("${response.get-processor-out.filename}")
    private String getProcessorOutResponseFilename;

    @Value("${response.get-processor-outs-of-request.filename}")
    private String getProcessorOutsOfRequestResponseFilename;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private ZorthTranslatorClient zorthTranslatorClient;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withInitScript("sql/processor_out.sql");

    private static WireMockRuntimeInfo wireMockRuntimeInfo;

    @BeforeAll
    static void beforeAll(WireMockRuntimeInfo info) {
        postgres.start();
        wireMockRuntimeInfo = info;
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port + contextPath;
        RestAssured.defaultParser = Parser.JSON;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2" + postgres.getJdbcUrl().substring(1));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
        registry.add("wiremock.url", wireMockRuntimeInfo::getHttpBaseUrl);
    }

    @Test
    public void executeWithBadRequestBody() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body("{}")
                .when()
                .post("/execute")
                .then()
                .extract()
                .response();
        assertEquals(400, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void executeInvalidRequest() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-type", "application/json")
                .withBodyFile(compilerOutNotFoundResponseFilename)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body(new ExecuteRequest(new String[]{"k", "2", "3"}, userRequestId))
                .when()
                .post("/execute")
                .then()
                .extract()
                .response();
        assertEquals(404, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void executeOwnRequestAsUser() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename2)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body(new ExecuteRequest(new String[]{"k", "2", "3"}, userRequestId))
                .when()
                .post("/execute")
                .then()
                .extract()
                .response();
        JsonPath successfulJsonPath = new JsonPath(getClass().getClassLoader()
                .getResourceAsStream("__files/" + getProcessorOutResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(successfulJsonPath.getString("input"), response.body().jsonPath().getString("input")),
                () -> assertEquals(successfulJsonPath.getString("tickLogs"), response.body().jsonPath().getString("tickLogs"))
        );
    }

    @Test
    public void executeNotOwnRequestAsUser() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + adminRequestId).willReturn(aResponse().withStatus(403)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body(new ExecuteRequest(new String[]{"k", "2", "3"}, adminRequestId))
                .when()
                .post("/execute")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void executeOwnRequestAsVip() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + vipRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename2)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .body(new ExecuteRequest(new String[]{"k", "2", "3"}, vipRequestId))
                .when()
                .post("/execute")
                .then()
                .extract()
                .response();
        JsonPath successfulJsonPath = new JsonPath(getClass().getClassLoader()
                .getResourceAsStream("__files/" + getProcessorOutResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(successfulJsonPath.getString("input"), response.body().jsonPath().getString("input")),
                () -> assertEquals(successfulJsonPath.getString("tickLogs"), response.body().jsonPath().getString("tickLogs"))
        );
    }

    @Test
    public void executeNotOwnRequestAsVip() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + adminRequestId).willReturn(aResponse().withStatus(403)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .body(new ExecuteRequest(new String[]{"k", "2", "3"}, adminRequestId))
                .when()
                .post("/execute")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void executeOwnRequestAsAdmin() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + adminRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename2)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .body(new ExecuteRequest(new String[]{"k", "2", "3"}, adminRequestId))
                .when()
                .post("/execute")
                .then()
                .extract()
                .response();
        JsonPath successfulJsonPath = new JsonPath(getClass().getClassLoader()
                .getResourceAsStream("__files/" + getProcessorOutResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(successfulJsonPath.getString("input"), response.body().jsonPath().getString("input")),
                () -> assertEquals(successfulJsonPath.getString("tickLogs"), response.body().jsonPath().getString("tickLogs"))
        );
    }

    @Test
    public void executeNotOwnRequestAsAdmin() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename2)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .body(new ExecuteRequest(new String[]{"k", "2", "3"}, userRequestId))
                .when()
                .post("/execute")
                .then()
                .extract()
                .response();
        JsonPath successfulJsonPath = new JsonPath(getClass().getClassLoader()
                .getResourceAsStream("__files/" + getProcessorOutResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(successfulJsonPath.getString("input"), response.body().jsonPath().getString("input")),
                () -> assertEquals(successfulJsonPath.getString("tickLogs"), response.body().jsonPath().getString("tickLogs"))
        );
    }

    @Test
    public void pipelineWithBadRequestBody() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body("{}")
                .when()
                .post("/pipeline")
                .then()
                .extract()
                .response();
        assertEquals(400, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void pipelineAsUser() {
        stubFor(post(contextPath + "/compile").willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(compileResponseFilename)));
        stubFor(get(contextPath + "/compiler-outs?request-id=" + adminRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename2)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body(new PipelineRequest(true, "variable a\n3 a !\na @", new String[]{"k", "2", "3"}))
                .when()
                .post("/pipeline")
                .then()
                .extract()
                .response();
        JsonPath successfulJsonPath = new JsonPath(getClass().getClassLoader()
                .getResourceAsStream("__files/" + getProcessorOutResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(successfulJsonPath.getString("input"), response.body().jsonPath().getString("input")),
                () -> assertEquals(successfulJsonPath.getString("tickLogs"), response.body().jsonPath().getString("tickLogs"))
        );
    }

    @Test
    public void pipelineAsVip() {
        stubFor(post(contextPath + "/compile").willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(compileResponseFilename)));
        stubFor(get(contextPath + "/compiler-outs?request-id=" + adminRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename2)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .body(new PipelineRequest(true, "variable a\n3 a !\na @", new String[]{"k", "2", "3"}))
                .when()
                .post("/pipeline")
                .then()
                .extract()
                .response();
        JsonPath successfulJsonPath = new JsonPath(getClass().getClassLoader()
                .getResourceAsStream("__files/" + getProcessorOutResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(successfulJsonPath.getString("input"), response.body().jsonPath().getString("input")),
                () -> assertEquals(successfulJsonPath.getString("tickLogs"), response.body().jsonPath().getString("tickLogs"))
        );
    }

    @Test
    public void pipelineAsAdmin() {
        stubFor(post(contextPath + "/compile").willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(compileResponseFilename)));
        stubFor(get(contextPath + "/compiler-outs?request-id=" + adminRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename2)));
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .body(new PipelineRequest(true, "variable a\n3 a !\na @", new String[]{"k", "2", "3"}))
                .when()
                .post("/pipeline")
                .then()
                .extract()
                .response();
        JsonPath successfulJsonPath = new JsonPath(getClass().getClassLoader()
                .getResourceAsStream("__files/" + getProcessorOutResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(successfulJsonPath.getString("input"), response.body().jsonPath().getString("input")),
                () -> assertEquals(successfulJsonPath.getString("tickLogs"), response.body().jsonPath().getString("tickLogs"))
        );
    }

    @Test
    public void getProcessorOutsOfInvalidRequest() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(404)
                .withHeader("Content-type", "application/json")
                .withBodyFile(compilerOutNotFoundResponseFilename)));
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/processor-outs?request-id=" + userRequestId)
                .then()
                .extract()
                .response();
        assertEquals(404, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getProcessorOutsOfExecutedRequest() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename3)));
        /* Сначала вызовем execute, чтобы занести данные в базу */
        given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body(new ExecuteRequest(new String[]{"k", "2", "3"}, userRequestId))
                .when()
                .post("/execute");
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/processor-outs?request-id=" + userRequestId)
                .then()
                .extract()
                .response();
        JsonPath successfulJsonPath = new JsonPath(getClass().getClassLoader()
                .getResourceAsStream("__files/" + getProcessorOutsOfRequestResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(successfulJsonPath.getString("[0].input"),
                        response.body().jsonPath().getString("[0].input")),
                () -> assertEquals(successfulJsonPath.getList("[0].tickLogs"),
                        response.body().jsonPath().getList("[0].tickLogs"))
        );
    }

    @Test
    public void getProcessorOutsOfNotExecutedRequest() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename1)));
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/processor-outs?request-id=" + userRequestId)
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().print()),
                () -> assertEquals("[]", response.body().asString())
        );
    }

    @Test
    public void testCircuitBreaker() throws InterruptedException {
        /* Включаем логирование событий на CB и сбрасываем его */
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("ZorthTranslatorClientCB");
        cb.reset();
        cb.getEventPublisher()
                .onError(System.out::println)
                .onSuccess(System.out::println)
                .onCallNotPermitted(System.out::println)
                .onStateTransition(System.out::println);

        /* Тестируем CLOSED с переходом в OPEN.
         * Переход должен произойти, если 2 вызова из 5 завершатся с ошибкой. */
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename1)));
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> zorthTranslatorClient.getCompilerOutOfRequest(userRequestId, userUsername, userAuthorities).block());
        }
        WireMock.reset(); // сбрасываем для нового stubFor
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse().withStatus(503)));
        for (int i = 0; i < 3; i++) {
            if (i != 2) {
                assertThrows(ResponseStatusException.class, () -> zorthTranslatorClient.getCompilerOutOfRequest(userRequestId, userUsername, userAuthorities).block());
            } else {
                assertThrows(CallNotPermittedException.class, () -> zorthTranslatorClient.getCompilerOutOfRequest(userRequestId, userUsername, userAuthorities).block());
            }
        }

        /* Тестируем HALF-OPEN c возвратом в OPEN.
         * Он должен произойти тогда, когда оба вызова в состоянии HALF-OPEN не будут успешны. */
        System.out.println("Sleeping for 10 seconds...");
        Thread.sleep(10000); // спим 10с, чтобы осуществился переход из OPEN в HALF-OPEN
        for (int i = 0; i < 3; i++) {
            if (i != 2) {
                assertThrows(ResponseStatusException.class, () -> zorthTranslatorClient.getCompilerOutOfRequest(userRequestId, userUsername, userAuthorities).block());
            } else {
                assertThrows(CallNotPermittedException.class, () -> zorthTranslatorClient.getCompilerOutOfRequest(userRequestId, userUsername, userAuthorities).block());
            }
        }

        /* Тестируем HALF-OPEN c возвратом в CLOSED.
         * Он должен произойти тогда, когда оба вызова в состоянии HALF-OPEN будут успешны. */
        System.out.println("Sleeping for 10 seconds...");
        Thread.sleep(10000); // спим 10с, чтобы осуществился переход из OPEN в HALF-OPEN
        WireMock.reset(); // сбрасываем для нового stubFor
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename1)));
        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(() -> zorthTranslatorClient.getCompilerOutOfRequest(userRequestId, userUsername, userAuthorities).block());
        }

        /* Сбрасываем CB */
        cb.reset();
    }
}