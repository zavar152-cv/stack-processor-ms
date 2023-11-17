package ru.itmo.zavar.highloadproject;

import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.itmo.zavar.highloadproject.dto.outer.request.ChangeRoleRequest;
import ru.itmo.zavar.highloadproject.dto.outer.request.SignInRequest;
import ru.itmo.zavar.highloadproject.dto.outer.request.SignUpRequest;
import ru.itmo.zavar.highloadproject.dto.outer.response.JwtAuthenticationResponse;
import ru.itmo.zavar.highloadproject.entity.security.RoleEntity;
import ru.itmo.zavar.highloadproject.entity.security.UserEntity;
import ru.itmo.zavar.highloadproject.repo.RoleRepository;
import ru.itmo.zavar.highloadproject.service.AuthenticationService;
import ru.itmo.zavar.highloadproject.service.JwtService;
import ru.itmo.zavar.highloadproject.util.RoleConstants;

import java.util.Optional;

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
public class AuthAndUserTests {

    @LocalServerPort
    private Integer port;

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:latest"
    );

    @Autowired
    public RoleRepository roleRepository;
    @Autowired
    public AuthenticationService authenticationService;
    @Autowired
    public JwtService jwtService;

    @Value("${admin.username}")
    private String adminUsername;
    @Value("${admin.password}")
    private String adminPassword;
    @Value("${test.username}")
    private String testUsername;
    @Value("${test.password}")
    private String testPassword;

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

    @Test
    @Order(1)
    public void signInAdminWithValidCredentials() {
        /*
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(adminUsername);
        Assertions.assertAll(
                () -> Assertions.assertEquals(jwtService.extractUserName(adminToken), adminUsername),
                () -> Assertions.assertTrue(jwtService.isTokenValid(adminToken, userDetails))
        );
         */
    }

    @Test
    @Order(2)
    public void signInAdminWithBadCredentials() {
        Assertions.assertThrows(BadCredentialsException.class, () -> {
            authenticationService.signIn(adminUsername, adminPassword + "pass");
        });
    }

    @Test
    @Order(3)
    public void signInAdminWithUnknownUsername() {
        Assertions.assertThrows(BadCredentialsException.class, () -> {
            authenticationService.signIn("invalid", "passpass");
        });
    }

    @Test
    @Order(4)
    public void addUserWithValidCredentialsFromAdmin() {
        /*
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new SignUpRequest(testUsername, testPassword))
                .when()
                .post("/api/v1/user/addUser")
                .then()
                .extract();
        Optional<UserEntity> userbyUsername = userRepository.findByUsername(testUsername);
        Assertions.assertAll(
                () -> Assertions.assertTrue(userbyUsername.isPresent()),
                () -> Assertions.assertEquals(HttpStatus.CREATED.value(), response.response().getStatusCode())
        );
        userRepository.deleteById(userbyUsername.get().getId());
         */
    }

    @Test
    @Order(5)
    public void addUserWithValidCredentialsNotFromAdmin() {
        /*
        authenticationService.addUser(testUsername, testPassword);
        String token = authenticationService.signIn(testUsername, testPassword);

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + token)
                .and()
                .body(new SignUpRequest(testUsername + "2", testPassword))
                .when()
                .post("/api/v1/user/addUser")
                .then()
                .extract();
        Optional<UserEntity> userbyUsername = userRepository.findByUsername(testUsername + "2");
        Assertions.assertAll(
                () -> Assertions.assertTrue(userbyUsername.isEmpty()),
                () -> Assertions.assertEquals(HttpStatus.FORBIDDEN.value(), response.response().getStatusCode())
        );
        userRepository.findByUsername("justdan").ifPresent(userEntity -> userRepository.deleteById(userEntity.getId()));
         */
    }

    @Test
    @Order(6)
    public void addUserWithInvalidCredentials() {
        /*
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new SignUpRequest("name", "pass"))
                .when()
                .post("/api/v1/user/addUser")
                .then()
                .extract();
        Optional<UserEntity> userbyUsername = userRepository.findByUsername("name");
        Assertions.assertAll(
                () -> Assertions.assertTrue(userbyUsername.isEmpty()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.response().getStatusCode())
        );
         */
    }

    @Test
    @Order(7)
    public void addUserWithSameName() {
        /*
        authenticationService.addUser(testUsername, testPassword);
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new SignUpRequest(testUsername, testPassword))
                .when()
                .post("/api/v1/user/addUser")
                .then()
                .extract();
        Optional<UserEntity> userbyUsername = userRepository.findByUsername(testUsername);
        Assertions.assertAll(
                () -> Assertions.assertTrue(userbyUsername.isPresent()),
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.response().getStatusCode())
        );
        userRepository.deleteById(userbyUsername.get().getId());
         */
    }

    @Test
    @Order(8)
    public void signInFromApiWithValidCredentials() {
        /*
        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .body(new SignInRequest(adminUsername, adminPassword))
                .when()
                .post("/api/v1/auth/signIn")
                .then()
                .extract();
        Assertions.assertAll(
                () -> {
                    JwtAuthenticationResponse authenticationResponse = response.as(JwtAuthenticationResponse.class);
                    UserDetails userDetails = userService.userDetailsService().loadUserByUsername(adminUsername);
                    Assertions.assertTrue(jwtService.isTokenValid(authenticationResponse.token(), userDetails));
                },
                () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode())
        );
         */
    }

    @Test
    @Order(9)
    public void signInFromApiWithInvalidCredentials() {
        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .body(new SignInRequest(adminUsername + "name", adminPassword))
                .when()
                .post("/api/v1/auth/signIn")
                .then()
                .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), response.response().getStatusCode())
        );
    }

    @Test
    @Order(9)
    public void changeRoleFromAdmin() {
        /*
        authenticationService.addUser(testUsername, testPassword);
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        String role = RoleConstants.ADMIN;

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new ChangeRoleRequest(testUsername, role))
                .when()
                .post("/api/v1/user/changeRole")
                .then()
                .extract();
        Optional<UserEntity> userbyUsername = userRepository.findByUsername(testUsername);
        Optional<RoleEntity> roleAdmin = roleRepository.findByName(role);
        if (userbyUsername.isPresent() & roleAdmin.isPresent()) {
            Assertions.assertAll(
                    () -> Assertions.assertEquals(HttpStatus.OK.value(), response.response().getStatusCode()),
                    () -> {
                        UserEntity userEntity = userbyUsername.get();
                        Assertions.assertEquals(1, userEntity.getRoles().size());
                    },
                    () -> {
                        UserEntity userEntity = userbyUsername.get();
                        Assertions.assertTrue(userEntity.getRoles().contains(roleAdmin.get()));
                    }
            );
        } else {
            Assertions.fail("User or role not found");
        }
        userRepository.deleteById(userbyUsername.get().getId());
         */
    }

    @Test
    @Order(10)
    public void changeRoleForInvalidUserFromAdmin() {
        String adminToken = authenticationService.signIn(adminUsername, adminPassword);
        String role = RoleConstants.ADMIN;

        ExtractableResponse<Response> response = given()
                .header("Content-type", "application/json")
                .and()
                .header("Authorization", "Bearer " + adminToken)
                .and()
                .body(new ChangeRoleRequest(testUsername, role))
                .when()
                .post("/api/v1/user/changeRole")
                .then()
                .extract();
        Assertions.assertAll(
                () -> Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), response.response().getStatusCode())
        );

    }
}
