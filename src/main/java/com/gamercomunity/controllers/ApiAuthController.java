package com.gamercomunity.controllers;

import com.gamercomunity.config.JwtConfig;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ApiAuthController {
    private static final long TOKEN_DURATION_SECONDS = 1800;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    public ApiAuthController(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest login) {
        if (login.username() == null || login.username().isBlank() || login.username().length() > 100
                || login.password() == null || login.password().isBlank()
                || login.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credenciales inválidas");
        }

        Authentication user;
        try {
            user = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(login.username(), login.password()));
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject(user.getName())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(TOKEN_DURATION_SECONDS))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new TokenResponse(token));
    }

    @GetMapping("/perfil")
    public ProfileResponse perfil(Authentication authentication) {
        return new ProfileResponse(authentication.getName());
    }

    public record LoginRequest(String username, String password) {}
    public record TokenResponse(String token) {}
    public record ProfileResponse(String username) {}
}
