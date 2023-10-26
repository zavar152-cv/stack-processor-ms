package ru.itmo.zavar.highloadproject;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.itmo.zavar.InstructionCode;
import ru.itmo.zavar.exception.ZorthException;
import ru.itmo.zavar.highloadproject.dto.request.ExecuteRequest;
import ru.itmo.zavar.highloadproject.dto.request.GetDebugMessagesRequest;
import ru.itmo.zavar.highloadproject.dto.request.GetProcessorOutRequest;
import ru.itmo.zavar.highloadproject.dto.request.SignUpRequest;
import ru.itmo.zavar.highloadproject.dto.response.CompilerOutResponse;
import ru.itmo.zavar.highloadproject.dto.response.DebugMessagesResponse;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.CompilerOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.DebugMessagesEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.ProcessorOutEntity;
import ru.itmo.zavar.highloadproject.entity.zorth.RequestEntity;
import ru.itmo.zavar.highloadproject.repo.*;
import ru.itmo.zavar.highloadproject.service.AuthenticationService;
import ru.itmo.zavar.highloadproject.service.ZorthTranslatorService;
import ru.itmo.zavar.highloadproject.util.ZorthUtil;
import ru.itmo.zavar.zorth.ProgramAndDataDto;
import ru.itmo.zavar.zorth.ZorthTranslator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
                "spring.jpa.hibernate.ddl-auto=create",
                "spring.jpa.generate-ddl=true",
                "spring.jpa.show-sql=true",
                "spring.liquibase.enabled=false"
        }
)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ZorthTests {

    @LocalServerPort
    private Integer port;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        RestAssured.defaultParser = Parser.JSON;
    }

    private static String debugOutputToCompare;
    private static byte[] dataToCompare;
    private static byte[] programToCompare;

    @BeforeAll
    static void prepareInfoToCompare() throws IOException {
        String programText = IOUtils.toString(
                Objects.requireNonNull(ZorthTests.class.getResourceAsStream("var.zorth")),
                StandardCharsets.UTF_8
        );

        ZorthTranslator translator = new ZorthTranslator(null, null, true);
        translator.compileFromString(true, programText);
        translator.linkage(true);
        debugOutputToCompare = String.join("\n", translator.getDebugMessages());

        ProgramAndDataDto out = translator.getCompiledProgramAndDataInBytes();

        ArrayList<Byte> data = new ArrayList<>();
        out.data().forEach(bytes -> data.addAll(Arrays.stream(bytes).toList()));
        Byte[] dataArray = new Byte[data.size()];
        data.toArray(dataArray);
        dataToCompare = ArrayUtils.toPrimitive(dataArray);

        ArrayList<Byte> program = new ArrayList<>();
        out.program().forEach(bytes -> program.addAll(Arrays.stream(bytes).toList()));
        Byte[] programArray = new Byte[program.size()];
        program.toArray(programArray);
        programToCompare = ArrayUtils.toPrimitive(programArray);
    }

    @Autowired
    public UserRepository userRepository;
    @Autowired
    public ZorthTranslatorService zorthTranslatorService;
    @Autowired
    private CompilerOutRepository compilerOutRepository;
    @Autowired
    private DebugMessagesRepository debugMessagesRepository;
    @Autowired
    private RequestRepository requestRepository;
    @Autowired
    private ProcessorOutRepository processorOutRepository;
    @Autowired
    private AuthenticationService authenticationService;

    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.password}")
    private String adminPassword;
    @Value("${test.username}")
    private String testUsername;
    @Value("${test.password}")
    private String testPassword;

    @Test
    @Order(1)
    public void compileAndLinkageWithValidProgramAndDebugAndAdminRole() throws IOException {
        String program = IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResourceAsStream("var.zorth")),
                StandardCharsets.UTF_8
        );

        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        zorthTranslatorService.compileAndLinkage(true, program, adminEntity);
        RequestEntity requestEntity = adminEntity.getRequests().get(0);
        DebugMessagesEntity debugMessagesEntity = debugMessagesRepository.findByRequest(requestEntity).orElseThrow();
        adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        CompilerOutEntity compilerOutEntity = compilerOutRepository.findByRequest(requestEntity).orElseThrow();
        UserEntity finalAdminEntity = adminEntity;
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, finalAdminEntity.getRequests().size()),
                () -> Assertions.assertEquals(debugOutputToCompare, debugMessagesEntity.getText()),
                () -> Assertions.assertArrayEquals(dataToCompare, compilerOutEntity.getData()),
                () -> Assertions.assertArrayEquals(programToCompare, compilerOutEntity.getProgram())
        );
    }

    @Test
    @Order(2)
    public void compileAndLinkageWithValidProgramAndDebugAndUserRole() throws IOException {
        String program = IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResourceAsStream("var.zorth")),
                StandardCharsets.UTF_8
        );
        authenticationService.addUser(testUsername, testPassword);
        UserEntity userEntity = userRepository.findByUsername(testUsername).orElseThrow();
        zorthTranslatorService.compileAndLinkage(true, program, userEntity);
        RequestEntity requestEntity = userEntity.getRequests().get(0);
        DebugMessagesEntity debugMessagesEntity = debugMessagesRepository.findByRequest(requestEntity).orElseThrow();
        userEntity = userRepository.findByUsername(testUsername).orElseThrow();

        CompilerOutEntity compilerOutEntity = compilerOutRepository.findByRequest(requestEntity).orElseThrow();
        UserEntity finalUserEntity = userEntity;
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, finalUserEntity.getRequests().size()),
                () -> Assertions.assertEquals(debugOutputToCompare, debugMessagesEntity.getText()),
                () -> Assertions.assertArrayEquals(dataToCompare, compilerOutEntity.getData()),
                () -> Assertions.assertArrayEquals(programToCompare, compilerOutEntity.getProgram())
        );
    }

    @Test
    @Order(3)
    public void compileAndLinkageWithUserRoleCountCheck() throws IOException {
        String program = IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResourceAsStream("var.zorth")),
                StandardCharsets.UTF_8
        );
        UserEntity userEntity = userRepository.findByUsername(testUsername).orElseThrow();
        zorthTranslatorService.compileAndLinkage(true, program, userEntity);
        userEntity = userRepository.findByUsername(testUsername).orElseThrow();
        zorthTranslatorService.compileAndLinkage(false, program, userEntity);
        userEntity = userRepository.findByUsername(testUsername).orElseThrow();
        zorthTranslatorService.compileAndLinkage(true, program, userEntity);
        UserEntity finalUserEntity = userEntity;
        Assertions.assertEquals(1, finalUserEntity.getRequests().size());
    }

    @Test
    @Order(4)
    public void compileAndLinkageWithAdminRoleCountCheck() throws IOException {
        String program = IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResourceAsStream("var.zorth")),
                StandardCharsets.UTF_8
        );
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        int expected = adminEntity.getRequests().size() + 3;
        zorthTranslatorService.compileAndLinkage(true, program, adminEntity);
        adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        zorthTranslatorService.compileAndLinkage(false, program, adminEntity);
        adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        zorthTranslatorService.compileAndLinkage(true, program, adminEntity);
        UserEntity finalUserEntity = adminEntity;
        Assertions.assertEquals(expected, finalUserEntity.getRequests().size());
    }

    @Test
    @Order(5)
    public void compileAndLinkageWithVipRoleCountCheck() throws IOException {
        String program = IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResourceAsStream("var.zorth")),
                StandardCharsets.UTF_8
        );
        authenticationService.changeRole(testUsername, "ROLE_VIP");
        UserEntity userEntity = userRepository.findByUsername(testUsername).orElseThrow();
        int expected = userEntity.getRequests().size() + 3;
        zorthTranslatorService.compileAndLinkage(true, program, userEntity);
        userEntity = userRepository.findByUsername(testUsername).orElseThrow();
        zorthTranslatorService.compileAndLinkage(false, program, userEntity);
        userEntity = userRepository.findByUsername(testUsername).orElseThrow();
        zorthTranslatorService.compileAndLinkage(true, program, userEntity);
        UserEntity finalUserEntity = userEntity;
        Assertions.assertEquals(expected, finalUserEntity.getRequests().size());
        authenticationService.changeRole(testUsername, "ROLE_USER");
    }

    @Test
    @Order(6)
    public void compileAndLinkageWithInvalidProgramAndUserRole() throws IOException {
        String program = IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResourceAsStream("varInvalid.zorth")),
                StandardCharsets.UTF_8
        );
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        int expected = adminEntity.getRequests().size();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            zorthTranslatorService.compileAndLinkage(true, program, adminEntity);
        });
        Assertions.assertEquals(expected, adminEntity.getRequests().size());
    }

    @Test
    @Order(7)
    public void compileAndLinkageWithValidProgramAndNoDebugAndUserRole() throws IOException {
        String program = IOUtils.toString(
                Objects.requireNonNull(this.getClass().getResourceAsStream("var.zorth")),
                StandardCharsets.UTF_8
        );
        UserEntity userEntity = userRepository.findByUsername(testUsername).orElseThrow();
        zorthTranslatorService.compileAndLinkage(false, program, userEntity);
        RequestEntity requestEntity = userEntity.getRequests().get(0);
        Optional<DebugMessagesEntity> debugMessagesEntity = debugMessagesRepository.findByRequest(requestEntity);
        userEntity = userRepository.findByUsername(testUsername).orElseThrow();

        CompilerOutEntity compilerOutEntity = compilerOutRepository.findByRequest(requestEntity).orElseThrow();
        UserEntity finalUserEntity = userEntity;
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, finalUserEntity.getRequests().size()),
                () -> Assertions.assertTrue(debugMessagesEntity.isEmpty()),
                () -> Assertions.assertArrayEquals(dataToCompare, compilerOutEntity.getData()),
                () -> Assertions.assertArrayEquals(programToCompare, compilerOutEntity.getProgram())
        );
    }

    @Test
    @Order(8)
    public void executeFromAdminWithValidRequest() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        Long id = adminEntity.getRequests().get(0).getId();

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new ExecuteRequest(new String[]{"k", "2", "3"}, id))
                .when()
                .post("/api/v1/zorth/execute")
                .then()
                .extract();
        Assertions.assertEquals(200, response.response().statusCode());
    }

    @Test
    @Order(9)
    public void executeFromAdminWithInvalidRequestAndGot403() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity testUserEntity = userRepository.findByUsername(testUsername).orElseThrow();
        Long id = testUserEntity.getRequests().get(0).getId();

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new ExecuteRequest(new String[]{"k", "2", "3"}, id))
                .when()
                .post("/api/v1/zorth/execute")
                .then()
                .extract();
        Assertions.assertEquals(403, response.response().statusCode());
    }

    @Test
    @Order(10)
    public void getAllProcessorOutWithAccessCountCheck() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        Long id = adminEntity.getRequests().get(0).getId();
        RequestEntity requestEntity = requestRepository.findById(id).orElseThrow();
        CompilerOutEntity compilerOutEntity = compilerOutRepository.findByRequest(requestEntity).orElseThrow();
        List<ProcessorOutEntity> allByCompilerOut = processorOutRepository.findAllByCompilerOut(compilerOutEntity);

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new GetProcessorOutRequest(id))
                .when()
                .get("/api/v1/zorth/getAllProcessorOut")
                .then()
                .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(200, response.response().statusCode()),
                () -> Assertions.assertEquals(allByCompilerOut.size(), Integer.valueOf(response.response().getHeader("Requests-Count")))
        );
    }

    @Test
    @Order(11)
    public void getAllProcessorOutWithoutAccess() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity adminEntity = userRepository.findByUsername(testUsername).orElseThrow();
        Long id = adminEntity.getRequests().get(0).getId();
        RequestEntity requestEntity = requestRepository.findById(id).orElseThrow();
        CompilerOutEntity compilerOutEntity = compilerOutRepository.findByRequest(requestEntity).orElseThrow();
        List<ProcessorOutEntity> allByCompilerOut = processorOutRepository.findAllByCompilerOut(compilerOutEntity);

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new GetProcessorOutRequest(id))
                .when()
                .get("/api/v1/zorth/getAllProcessorOut")
                .then()
                .extract();
        Assertions.assertEquals(403, response.response().statusCode());
    }

    @Test
    @Order(12)
    public void getDebugMessagesWithValidId() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        RequestEntity requestEntity = adminEntity.getRequests().get(0);
        DebugMessagesEntity debugMessagesEntity = debugMessagesRepository.findByRequest(requestEntity).orElseThrow();

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .when()
                .get("/api/v1/zorth/getDebugMessages/" + debugMessagesEntity.getId())
                .then()
                .extract();
        DebugMessagesResponse debugMessagesResponse = response.as(DebugMessagesResponse.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(200, response.response().statusCode()),
                () -> Assertions.assertArrayEquals(debugMessagesEntity.getText().split("\n"), debugMessagesResponse.text())
        );
    }

    @Test
    @Order(13)
    public void getDebugMessagesWithInvalidId() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .when()
                .get("/api/v1/zorth/getDebugMessages/" + 45645646)
                .then()
                .extract();
        Assertions.assertEquals(404, response.response().statusCode());
    }

    @Test
    @Order(14)
    public void getCompilerOutWithValidId() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        RequestEntity requestEntity = adminEntity.getRequests().get(0);
        CompilerOutEntity compilerOutEntity = compilerOutRepository.findByRequest(requestEntity).orElseThrow();

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .when()
                .get("/api/v1/zorth/getCompilerOut/" + compilerOutEntity.getId())
                .then()
                .extract();
        CompilerOutResponse compilerOutResponse = response.as(CompilerOutResponse.class);

        ArrayList<Long> program = new ArrayList<>();
        ArrayList<Long> data = new ArrayList<>();

        byte[] bytesProg = compilerOutEntity.getProgram();
        List<Byte[]> instructions = ZorthUtil.splitArray(ArrayUtils.toObject(bytesProg));
        instructions.forEach(bInst -> program.add(InstructionCode.bytesToLong(ArrayUtils.toPrimitive(bInst))));

        byte[] bytesData = compilerOutEntity.getData();
        List<Byte[]> datas = ZorthUtil.splitArray(ArrayUtils.toObject(bytesData));
        datas.forEach(bData -> data.add(InstructionCode.bytesToLong(ArrayUtils.toPrimitive(bData))));

        Assertions.assertAll(
                () -> Assertions.assertEquals(200, response.response().statusCode()),
                () -> Assertions.assertEquals(data, compilerOutResponse.data()),
                () -> Assertions.assertEquals(program, compilerOutResponse.program())
        );
    }

    @Test
    @Order(15)
    public void getCompilerOutWithInvalidId() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .when()
                .get("/api/v1/zorth/getCompilerOut/" + 86736)
                .then()
                .extract();
        Assertions.assertEquals(404, response.response().statusCode());
    }

    @Test
    @Order(16)
    @Disabled
    public void getAllCompilerOut() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .when()
                .get("/api/v1/zorth/getAllCompilerOut/")
                .then()
                .extract();
        Page<?> page = response.as(Page.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(200, response.response().statusCode()),
                () -> Assertions.assertEquals(3, page.getTotalElements()),
                () -> Assertions.assertEquals(1, page.getTotalPages())
        );
    }

    @Test
    @Order(17)
    @Disabled
    public void getAllDebugMessages() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .when()
                .get("/api/v1/zorth/getAllDebugMessages/")
                .then()
                .extract();
        PageImpl<?> page = response.as(PageImpl.class);

        Assertions.assertAll(
                () -> Assertions.assertEquals(200, response.response().statusCode()),
                () -> Assertions.assertEquals(3, page.getTotalElements()),
                () -> Assertions.assertEquals(1, page.getTotalPages())
        );
    }

    @Test
    @Order(18)
    public void getAllRequestsById() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        Long id = adminEntity.getId();
        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .when()
                .get("/api/v1/zorth/getAllRequests/" + id)
                .then()
                .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(200, response.response().statusCode()),
                () -> Assertions.assertEquals(adminEntity.getRequests().size(), Integer.valueOf(response.response().getHeader("Requests-Count")))
        );
    }

    @Test
    @Order(19)
    public void getAllRequestsByInvalidId() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .when()
                .get("/api/v1/zorth/getAllRequests/" + 7345)
                .then()
                .extract();
        Assertions.assertEquals(404, response.response().statusCode());
    }

    @Test
    @Order(20)
    public void getRequestsByCurrentUser() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .when()
                .get("/api/v1/zorth/getRequests")
                .then()
                .extract();
        ArrayList<?> responseList = response.as(ArrayList.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(200, response.response().statusCode()),
                () -> Assertions.assertEquals(adminEntity.getRequests().size(), Integer.valueOf(response.response().getHeader("Requests-Count"))),
                () -> Assertions.assertEquals(adminEntity.getRequests().size(), responseList.size())
        );
    }

    @Test
    @Order(21)
    public void getDebugMessagesOfRequestWithValidId() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserEntity adminEntity = userRepository.findByUsername(adminUsername).orElseThrow();
        RequestEntity requestEntity = adminEntity.getRequests().get(0);
        DebugMessagesEntity debugMessagesEntity = zorthTranslatorService.getDebugMessagesByRequestId(1L).orElseThrow();

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new GetDebugMessagesRequest(requestEntity.getId()))
                .when()
                .get("/api/v1/zorth/getDebugMessagesOfRequest")
                .then()
                .extract();
        DebugMessagesResponse debugMessagesResponse = response.as(DebugMessagesResponse.class);
        Assertions.assertAll(
                () -> Assertions.assertEquals(200, response.response().statusCode()),
                () -> Assertions.assertArrayEquals(debugMessagesEntity.getText().split("\n"), debugMessagesResponse.text())
        );
    }

    @Test
    @Order(22)
    public void getDebugMessagesOfRequestWithInvalidId() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new GetDebugMessagesRequest(45645L))
                .when()
                .get("/api/v1/zorth/getDebugMessagesOfRequest")
                .then()
                .extract();
        Assertions.assertEquals(404, response.response().statusCode());
    }

}
