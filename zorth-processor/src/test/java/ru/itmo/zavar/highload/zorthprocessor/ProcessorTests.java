package ru.itmo.zavar.highload.zorthprocessor;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
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
import org.testcontainers.containers.PostgreSQLContainer;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.ExecuteRequest;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.PipelineRequest;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.response.GetProcessorOutResponse;
import ru.itmo.zavar.highload.zorthprocessor.service.impl.ZorthProcessorServiceImpl;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@WireMockTest(httpPort = 7357)
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

    @Value("${response.get-all-processor-out.filename}")
    private String getAllProcessorOutResponseFilename;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withInitScript("sql/processor_out.sql");

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
        circuitBreakerRegistry.circuitBreaker("ZorthTranslatorClientCB").reset();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2" + postgres.getJdbcUrl().substring(1));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
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
                .withBodyFile(getCompilerOutResponseFilename1)));
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
                .withBodyFile(getCompilerOutResponseFilename1)));
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
                .withBodyFile(getCompilerOutResponseFilename1)));
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
                .withBodyFile(getCompilerOutResponseFilename1)));
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
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename1)));
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
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename1)));
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
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename1)));
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

    @Autowired
    ZorthProcessorServiceImpl processorService;

    @Test
    public void getProcessorOutOfInvalidRequest() {
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
    public void getProcessorOutOfExecutedRequest() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename2)));
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
                .getResourceAsStream("__files/" + getAllProcessorOutResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(successfulJsonPath.getList("", GetProcessorOutResponse.class).get(0).input(),
                        response.body().jsonPath().getList("", GetProcessorOutResponse.class).get(0).input()),
                () -> assertArrayEquals(successfulJsonPath.getList("", GetProcessorOutResponse.class).get(0).tickLogs(),
                        response.body().jsonPath().getList("", GetProcessorOutResponse.class).get(0).tickLogs())
        );
    }

    @Test
    public void getProcessorOutOfNotExecutedRequest() {
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename3)));
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
                () -> assertEquals("[]", response.body().jsonPath().getString(""))
        );
    }

    @Test
    public void testCircuitBreaker() throws InterruptedException {
        /* Тестируем CLOSED с переходом в OPEN.
         * Переход должен произойти, если 2 вызова из 5 завершатся с ошибкой. */
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename3)));
        for (int i = 0; i < 3; i++) {
            Response response = given()
                    .header("username", userUsername)
                    .header("authorities", userAuthorities)
                    .when()
                    .get("/processor-outs?request-id=" + userRequestId)
                    .then()
                    .extract()
                    .response();
            assertEquals(200, response.statusCode(), response.body().print());
        }
        WireMock.reset(); // сбрасываем для нового stubFor
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(503)));
        for (int i = 0; i < 3; i++) {
            Response response = given()
                    .header("username", userUsername)
                    .header("authorities", userAuthorities)
                    .when()
                    .get("/processor-outs?request-id=" + userRequestId)
                    .then()
                    .extract()
                    .response();
            assertEquals(i != 2 ? 503 : 500, response.statusCode(), response.body().print());
        }

        /* Тестируем HALF-OPEN c возвратом в OPEN.
         * Он должен произойти тогда, когда оба вызова в состоянии HALF-OPEN не будут успешны. */
        Thread.sleep(10000); // спим 10с, чтобы осуществился переход из OPEN в HALF-OPEN
        for (int i = 0; i < 3; i++) {
            Response response = given()
                    .header("username", userUsername)
                    .header("authorities", userAuthorities)
                    .when()
                    .get("/processor-outs?request-id=" + userRequestId)
                    .then()
                    .extract()
                    .response();
            assertEquals(i != 2 ? 503 : 500, response.statusCode(), response.body().print());
        }

        /* Тестируем HALF-OPEN c возвратом в CLOSED.
         * Он должен произойти тогда, когда оба вызова в состоянии HALF-OPEN будут успешны. */
        Thread.sleep(10000); // спим 10с, чтобы осуществился переход из OPEN в HALF-OPEN
        WireMock.reset(); // сбрасываем для нового stubFor
        stubFor(get(contextPath + "/compiler-outs?request-id=" + userRequestId).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-type", "application/json")
                .withBodyFile(getCompilerOutResponseFilename3)));
        for (int i = 0; i < 3; i++) {
            Response response = given()
                    .header("username", userUsername)
                    .header("authorities", userAuthorities)
                    .when()
                    .get("/processor-outs?request-id=" + userRequestId)
                    .then()
                    .extract()
                    .response();
            assertEquals(200, response.statusCode(), response.body().print());
        }
    }
}