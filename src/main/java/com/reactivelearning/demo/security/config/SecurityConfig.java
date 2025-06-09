package com.reactivelearning.demo.security.config;

import com.reactivelearning.demo.security.filters.CookieFilter;
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
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.cors.CorsConfiguration;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * The legacy filter.
     * - For use ONLY with the legacy CRUD operations.
     * @param http ServerHttpSecurity : A webflux specific filter chain/
     * @param location String : CORS source location
     * @return SecurityWebFilterChain : Webflux specific response
     */
    @Order(1)
    @Bean
    public SecurityWebFilterChain userFilterChain (
            ServerHttpSecurity http,
            @Value("${domain.name}") String location,
            CookieFilter cookieFilter) {

        return defaultConfig(location, http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/v1/users"))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/v1/users").authenticated()
                )
                .csrf(csrf -> csrf
                        .requireCsrfProtectionMatcher(exchange -> {
                            if (exchange.getRequest().getMethod().matches("GET")) {
                                return ServerWebExchangeMatcher.MatchResult.notMatch();
                            } else {
                                return ServerWebExchangeMatcher.MatchResult.match();
                            }
                        })));

    }

    /**
     * Security filter for /login and /register.
     * @param http ServerHttpSecurity: WebFlux-specific Web Filter
     * @param location String : CORS location
     * @return SecurityWebFilterChain : A fully configured filter chain
     */
    @Order(2)
    @Bean
    public SecurityWebFilterChain loginChain(
            ServerHttpSecurity http, @Value("${domain.name}") String location) {

        return defaultConfig(location, http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/api/v1/auth/**"))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                        .anyExchange().authenticated()
                )
                .csrf(csrf -> csrf.disable()));

    }

    /**
     * The default configuration
     * - If any requests hit a URL that does not fall within the set endpoints, it will use this
     * @param http ServerHttpSecurity : A webflux specific filter chain/
     * @param location String : CORS source location
     * @return SecurityWebFilterChain : Webflux specific response
     */
    @Order(3)
    @Bean
    public SecurityWebFilterChain defaultChain (
            ServerHttpSecurity http, @Value("${domain.name}") String location) {

        return defaultConfig(location, http
                .securityMatcher(new PathPatternParserServerWebExchangeMatcher("/**"))
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().denyAll()
                )
                .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())));

    }

    // Private methods

    /**
     * The duplicated configurations.
     * - Since there will be multiple endpoints with different filters, this allows them to only write what is
     * necessary.
     * @param http ServerHttpSecurity : A webflux specific filter chain/
     * @param location String : CORS source location
     * @return SecurityWebFilterChain : Webflux specific response
     */
    private SecurityWebFilterChain defaultConfig(
            String location, ServerHttpSecurity http) {
        return http
                .exceptionHandling((ServerHttpSecurity.ExceptionHandlingSpec exceptions) -> exceptions
                        .authenticationEntryPoint(authFailureExceptionHandler())
                )
                .cors(cors -> cors.configurationSource(exchange -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of(location));
                    config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .httpBasic(httpBasicSpec ->
                        httpBasicSpec.authenticationEntryPoint(authFailureExceptionHandler()))
                .build();
    }

    /**
     * The implementation for failing authentication and Spring Security passes the unauthenticated user to us.
     * @return ServerAuthenticationEntryPoint : A response in JSON.
     */
    private ServerAuthenticationEntryPoint authFailureExceptionHandler() {
        return (exchange, ex) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders()
                    .add("Content-Type", "application/json");
            return response.writeWith(Mono.just(response
                    .bufferFactory()
                    .wrap(String.format("{\"response\": \"%s\"}", ex.getMessage())
                            .getBytes(StandardCharsets.UTF_8))
            ));
        };
    }

    // Beans

    /**
     * A reactive implementation of basic username/password auth, provided by Spring Security.
     * @param userService Object of UserService, an injected Service-annotated class
     * @param encoder Object of PasswordEncoder, holds password hashing-specific details
     * @return ReactiveAuthenticationManager : A bean that handles basic auth
     */
    @Bean
    public ReactiveAuthenticationManager authenticationManager(UserService userService, PasswordEncoder encoder) {
        UserDetailsRepositoryReactiveAuthenticationManager manager = new UserDetailsRepositoryReactiveAuthenticationManager(userService);
        manager.setPasswordEncoder(encoder);
        return manager;
    }

}
