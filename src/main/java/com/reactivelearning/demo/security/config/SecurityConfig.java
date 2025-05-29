package com.reactivelearning.demo.security.config;

import com.reactivelearning.demo.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Order(1)
    @Bean
    public SecurityWebFilterChain userFilterChain (
            ServerHttpSecurity http, @Value("${domain.name}") String location) {

        return defaultConfig(location, http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/v1/users"))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/users").authenticated()
                )
                .csrf(csrf -> csrf
                        .requireCsrfProtectionMatcher(exchange -> {
                            if (exchange.getRequest().getMethod().matches("GET")) {
                                return ServerWebExchangeMatcher.MatchResult.match();
                            } else {
                                return ServerWebExchangeMatcher.MatchResult.notMatch();
                            }
                        })));

    }

    @Order(2)
    @Bean
    public SecurityWebFilterChain defaultChain (
            ServerHttpSecurity http, @Value("${domain.name}") String location) {

        return defaultConfig(location, http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/v1/**"))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().authenticated()
                )
                .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())));

    }

    private SecurityWebFilterChain defaultConfig(
            String location, ServerHttpSecurity http) {
        return http
                .exceptionHandling((ServerHttpSecurity.ExceptionHandlingSpec exceptions) -> exceptions
                        .authenticationEntryPoint((ServerWebExchange exchange, AuthenticationException ex) -> {
                            ServerHttpResponse response = exchange.getResponse();
                            response.setStatusCode(HttpStatus.UNAUTHORIZED);
                            response.getHeaders()
                                    .add("Content-Type", "application/json");
                            return response.writeWith(Mono.just(response
                                    .bufferFactory()
                                    .wrap(String.format("{\"response\": \"%s\"}", ex.getMessage())
                                            .getBytes(StandardCharsets.UTF_8))
                            ));
                        })
                )
                .cors(cors -> cors.configurationSource(exchange -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(location));
                    config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(UserService userService, PasswordEncoder encoder) {
        UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(userService);
        manager.setPasswordEncoder(encoder);
        return manager;
    }

}
