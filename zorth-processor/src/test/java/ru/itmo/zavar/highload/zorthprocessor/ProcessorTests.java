package ru.itmo.zavar.highload.zorthprocessor;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.ExecuteRequest;
import ru.itmo.zavar.highload.zorthprocessor.dto.outer.request.PipelineRequest;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    @Value("${response.get-compiler-out.filename}")
    private String getCompilerOutResponseFilename;

    @Value("${response.compiler-out-not-found.filename}")
    private String compilerOutNotFoundResponseFilename;

    @Value("${response.get-processor-out.filename}")
    private String getProcessorOutResponseFilename;

    @Value("${response.compile.filename}")
    private String compileResponseFilename;

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

    @BeforeAll
    static void prepareInfoToCompare() throws IOException {
        /*
        Gson gson = new Gson();
        String json = gson.toJson(input);
        JSONParser jsonParser = new JSONParser();
        JSONArray inputJson = (JSONArray) jsonParser.parse(json);

        ControlUnit controlUnit = new ControlUnit(dto.program(), dto.data(), inputJson, true);
        controlUnit.start();

        StringBuilder stringBuilder = new StringBuilder();
        controlUnit.getTickLog().forEach(tickLog -> {
            stringBuilder.append("\n");
            stringBuilder.append(tickLog.toString());
        });

        ProcessorOutEntity processorOutEntity = ProcessorOutEntity.builder()
                .tickLogs(stringBuilder.toString().getBytes())
                .compilerOutId(dto.id())
                .input(inputJson.toString().getBytes())
                .build();*/
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
                .withBodyFile(getCompilerOutResponseFilename)));
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
                .withBodyFile(getCompilerOutResponseFilename)));
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
                .withBodyFile(getCompilerOutResponseFilename)));
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
                .withBodyFile(getCompilerOutResponseFilename)));
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
                .withBodyFile(getCompilerOutResponseFilename)));
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
                .withBodyFile(getCompilerOutResponseFilename)));
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
                .withBodyFile(getCompilerOutResponseFilename)));
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

    // TODO: добавить проверку случаев, когда транслятор недоступен + тесты getProcessorOutOfRequest
}