package ru.itmo.zavar.highload.userservice;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.itmo.zavar.highload.userservice.dto.outer.request.AddUserRequest;
import ru.itmo.zavar.highload.userservice.dto.outer.request.ChangeRoleRequest;
import ru.itmo.zavar.highload.userservice.entity.security.UserEntity;
import ru.itmo.zavar.highload.userservice.mapper.UserEntityMapper;
import ru.itmo.zavar.highload.userservice.repo.RoleRepository;
import ru.itmo.zavar.highload.userservice.repo.UserRepository;
import ru.itmo.zavar.highload.userservice.service.impl.UserServiceImpl;
import ru.itmo.zavar.highload.userservice.util.RoleConstants;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserTests {
    @LocalServerPort
    private Integer port;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${user.username}")
    private String userUsername;

    @Value("${user.password}")
    private String userPassword;

    @Value("${user.authorities}")
    private String userAuthorities;

    @Value("${vip.username}")
    private String vipUsername;

    @Value("${vip.password}")
    private String vipPassword;

    @Value("${vip.authorities}")
    private String vipAuthorities;

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.authorities}")
    private String adminAuthorities;

    @Value("${admin.id}")
    private Long adminId;

    @Value("${response.get-role.filename}")
    private String getRoleResponseFilename;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserEntityMapper userEntityMapper;

    @Autowired
    private PasswordEncoder encoder;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest");

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
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    public void addUserWithBadRequestBody() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .body("{}")
                .when()
                .post("/users")
                .then()
                .extract()
                .response();
        assertEquals(400, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void addExistingUser() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .body(new AddUserRequest(adminUsername, userPassword))
                .when()
                .post("/users")
                .then()
                .extract()
                .response();
        assertEquals(400, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void addUserAsAdmin() {
        assertFalse(userRepository.findByUsername(userUsername).isPresent());
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .body(new AddUserRequest(userUsername, userPassword))
                .when()
                .post("/users")
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(201, response.statusCode(), response.body().prettyPrint()),
                () -> assertTrue(userRepository.findByUsername(userUsername).isPresent())
        );
    }

    @Test
    public void addUserAsUser() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body(new AddUserRequest(vipUsername, vipPassword))
                .when()
                .post("/users")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void addUserAsVip() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .body(new AddUserRequest(userUsername, userPassword))
                .when()
                .post("/users")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void changeRoleWithBadRequestBody() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .body("{}")
                .when()
                .put("/users/" + vipUsername + "/roles")
                .then()
                .extract()
                .response();
        assertEquals(400, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void changeRoleOfNotExistingUser() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .body(new ChangeRoleRequest(RoleConstants.ADMIN))
                .when()
                .put("/users/" + "ABOBA" + "/roles")
                .then()
                .extract()
                .response();
        assertEquals(404, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void changeRoleAsAdmin() {
        if (userRepository.findByUsername(vipUsername).isEmpty()) {
            userService.addUser(vipUsername, vipPassword);
        }
        assertEquals(roleRepository.findByName(RoleConstants.USER).orElseThrow(),
                userRepository.findByUsername(vipUsername).orElseThrow().getRoles().toArray()[0]);
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .body(new ChangeRoleRequest(RoleConstants.VIP))
                .when()
                .put("/users/" + vipUsername + "/roles")
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(roleRepository.findByName(RoleConstants.VIP).orElseThrow(),
                        userRepository.findByUsername(vipUsername).orElseThrow().getRoles().toArray()[0])
        );
    }

    @Test
    public void changeRoleAsUser() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .body(new ChangeRoleRequest(RoleConstants.ADMIN))
                .when()
                .put("/users/" + vipUsername + "/roles")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void changeRoleAsVip() {
        Response response = given()
                .header("Content-type", "application/json")
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .body(new ChangeRoleRequest(RoleConstants.ADMIN))
                .when()
                .put("/users/" + userUsername + "/roles")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getRequestsOfNotExistingUser() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/users/" + "ABOBA" + "/requests")
                .then()
                .extract()
                .response();
        assertEquals(404, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getOwnRequestsAsAdmin() {
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/users/" + adminUsername + "/requests")
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals("[]", response.body().asString())
        );
    }

    @Test
    public void getNotOwnRequestsAsAdmin() {
        if (userRepository.findByUsername(vipUsername).isEmpty()) {
            userService.addUser(vipUsername, vipPassword);
        }
        Response response = given()
                .header("username", adminUsername)
                .header("authorities", adminAuthorities)
                .when()
                .get("/users/" + vipUsername + "/requests")
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals("[]", response.body().asString())
        );
    }

    @Test
    public void getOwnRequestsAsUser() {
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/users/" + userUsername + "/requests")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getNotOwnRequestsAsUser() {
        Response response = given()
                .header("username", userUsername)
                .header("authorities", userAuthorities)
                .when()
                .get("/users/" + vipUsername + "/requests")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getOwnRequestsAsVip() {
        if (userRepository.findByUsername(vipUsername).isEmpty()) {
            userService.addUser(vipUsername, vipPassword);
        }
        Response response = given()
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .when()
                .get("/users/" + vipUsername + "/requests")
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals("[]", response.body().asString())
        );
    }

    @Test
    public void getNotOwnRequestsAsVip() {
        Response response = given()
                .header("username", vipUsername)
                .header("authorities", vipAuthorities)
                .when()
                .get("/users/" + userUsername + "/requests")
                .then()
                .extract()
                .response();
        assertEquals(403, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void saveUserWithBadRequestBody() {
        Response response = given()
                .header("Content-type", "application/json")
                .body("{}")
                .when()
                .put("/users")
                .then()
                .extract()
                .response();
        assertEquals(400, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void saveUser() {
        UserEntity admin = userRepository.findByUsername(adminUsername).orElseThrow();
        assertEquals(1, admin.getRoles().size());
        admin.getRoles().add(roleRepository.findByName(RoleConstants.VIP).orElseThrow());
        Response response = given()
                .header("Content-type", "application/json")
                .body(userEntityMapper.toDTO(admin))
                .when()
                .put("/users")
                .then()
                .extract()
                .response();
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(2, userRepository.findByUsername(adminUsername).orElseThrow().getRoles().size())
        );
    }

    @Test
    public void getUser() {
        Response response = given()
                .when()
                .get("/users/" + adminUsername)
                .then()
                .extract()
                .response();
        JsonPath roleJsonPath = new JsonPath(getClass().getClassLoader()
                .getResourceAsStream(getRoleResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(adminId, response.body().jsonPath().getLong("id")),
                () -> assertEquals(adminUsername, response.body().jsonPath().getString("username")),
                () -> assertTrue(encoder.matches(adminPassword, response.body().jsonPath().getString("password"))),
                () -> assertEquals(roleJsonPath.getString(""), response.body().jsonPath().getString("roles[0]")),
                () -> assertEquals("[]", response.body().jsonPath().getString("requests"))
        );
    }

    @Test
    public void getNotExistingUser() {
        Response response = given()
                .when()
                .get("/users/" + "aboba")
                .then()
                .extract()
                .response();
        assertEquals(404, response.statusCode(), response.body().prettyPrint());
    }

    @Test
    public void getRole() {
        Response response = given()
                .when()
                .get("/roles/" + RoleConstants.ADMIN)
                .then()
                .extract()
                .response();
        JsonPath roleJsonPath = new JsonPath(getClass().getClassLoader()
                .getResourceAsStream(getRoleResponseFilename));
        assertAll(
                () -> assertEquals(200, response.statusCode(), response.body().prettyPrint()),
                () -> assertEquals(roleJsonPath.prettify(), response.body().asPrettyString())
        );
    }

    @Test
    public void getNotExistingRole() {
        Response response = given()
                .when()
                .get("/roles/" + "ROLE_MODER")
                .then()
                .extract()
                .response();
        assertEquals(404, response.statusCode(), response.body().prettyPrint());
    }
}