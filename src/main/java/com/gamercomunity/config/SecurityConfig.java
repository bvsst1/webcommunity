package com.gamercomunity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain apiSecurity(HttpSecurity http, UserDetailsService usuarios) throws Exception {
        var loginApi = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/login");
        return http.securityMatcher("/api/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // El login JSON no crea sesión. Las demás escrituras requieren un Bearer explícito.
                .csrf(csrf -> csrf.ignoringRequestMatchers(loginApi, request -> {
                    String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
                    return authorization != null && authorization.startsWith("Bearer ");
                }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/eventos/*/multimedia").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(server -> server.jwt(jwt -> jwt.jwtAuthenticationConverter(token -> {
                    try {
                        var usuario = usuarios.loadUserByUsername(token.getSubject());
                        return new JwtAuthenticationToken(token, usuario.getAuthorities(), usuario.getUsername());
                    } catch (UsernameNotFoundException ex) {
                        throw new InvalidBearerTokenException("Usuario no disponible", ex);
                    }
                })))
                .build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
.policyDirectives("default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; object-src 'none'; frame-ancestors 'none'; base-uri 'self'; form-action 'self'")                )
            )

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/webjars/**",
                    "/favicon.ico"
                ).permitAll()

                .requestMatchers(
                    "/",
                    "/home",
                    "/login",
                    "/registro",
                    "/error"
                ).permitAll()

                .requestMatchers(HttpMethod.POST, "/registro").permitAll()
                .requestMatchers(HttpMethod.GET, "/gamer").permitAll()
                .requestMatchers(HttpMethod.GET, "/eventos/compartidos", "/eventos/compartidos/*").permitAll()
                .requestMatchers(HttpMethod.POST, "/eventos/*/multimedia").hasRole("ADMIN")

                .requestMatchers("/admin/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/home", true)
                .failureUrl("/login?error")
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
