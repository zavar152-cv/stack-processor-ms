package ru.itmo.zavar.highload.authservice.controller;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.itmo.zavar.highload.authservice.dto.inner.request.ValidateTokenRequest;
import ru.itmo.zavar.highload.authservice.dto.inner.response.ValidateTokenResponse;
import ru.itmo.zavar.highload.authservice.dto.outer.request.SignInRequest;
import ru.itmo.zavar.highload.authservice.dto.outer.response.SignInResponse;
import ru.itmo.zavar.highload.authservice.entity.security.UserEntity;
import ru.itmo.zavar.highload.authservice.service.AuthenticationService;
import ru.itmo.zavar.highload.authservice.util.SpringWebErrorModel;

@Tag(name = "Auth Service Controller")
@RestController
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Sign in",
            description = "This method returns new JWT-token (which user needs to get into the system)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful sign in",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignInResponse.class)
                    )}
            ),
            @ApiResponse(responseCode = "400", description = "Bad request: request body isn't valid",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized: bad credentials",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            ),
            @ApiResponse(responseCode = "404", description = "User doesn't exist",
                    content = {@Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SpringWebErrorModel.class)
                    )}
            )
    })
    @PostMapping("/sign-in")
    public ResponseEntity<SignInResponse> signIn(@Valid @RequestBody SignInRequest request) throws Exception {
        try {
            SignInResponse response = new SignInResponse(authenticationService.signIn(request.username(), request.password()));
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (InternalAuthenticationServiceException e) {
            throw (Exception) e.getCause();
        }
    }

    @Hidden
    @PostMapping("/token/validate")
    public ResponseEntity<ValidateTokenResponse> validateToken(@Valid @RequestBody ValidateTokenRequest request) {
        try {
            UserEntity user = authenticationService.validateToken(request.jwtToken());
            ValidateTokenResponse response = ValidateTokenResponse.builder()
                    .username(user.getUsername())
                    .authorities(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                    .build();
            return ResponseEntity.ok(response);
        } catch (JwtException | IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}
