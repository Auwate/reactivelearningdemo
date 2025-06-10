package com.reactivelearning.demo.integration.filter;

import com.auth0.jwt.JWT;
import com.reactivelearning.demo.entities.Role;
import com.reactivelearning.demo.entities.RoleType;
import com.reactivelearning.demo.entities.User;
import com.reactivelearning.demo.security.filters.CookieFilter;
import com.reactivelearning.demo.security.jwt.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CookieFilterTests {

    private final String secret;
    private final Logger logger = LoggerFactory.getLogger(CookieFilter.class);

    private CookieFilter cookieFilter;
    private JwtUtil jwtUtil;
    private User user;

    @Mock
    private ServerWebExchange exchange;

    @Mock
    private WebFilterChain chain;

    public CookieFilterTests() {
        this.secret = "IAOWJ8D9AJWDUIDMIUAM3AIUDN";
        this.jwtUtil = new JwtUtil(secret);
        this.cookieFilter = new CookieFilter(jwtUtil);
    }

    @BeforeEach
    void setup() {
        this.user = new User("test", "testpassword", "test");
        user.setId(UUID.randomUUID());
        user.setRoles(List.of(Role.of(RoleType.USER.name())));
    }

    /**
     * Given a valid JWT cookie, it should go through the pipeline
     */
    @Test
    void shouldCompleteGivenAValidJwt() {

        logger.info("shouldCompleteGivenAValidJwt: Starting");

        String jwt = jwtUtil.generateToken(user);
        MultiValueMap<String, HttpCookie> multiValueMap = MultiValueMap
                .fromSingleValue(
                        Map.of(
                                "reactive_authn_authz",
                                new HttpCookie(
                                        "reactive_authn_authz",
                                        jwt)));

        ServerHttpRequest request = mock();
        RequestPath path = mock();

        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(exchange.getRequest()).thenReturn(request);
        when(request.getPath()).thenReturn(path);
        when(path.value()).thenReturn("");
        when(request.getCookies()).thenReturn(multiValueMap);

        StepVerifier.create(cookieFilter.filter(exchange, chain))
                .expectComplete()
                .verify();

        verify(chain, times(1)).filter(exchange);
        verify(exchange, times(3)).getRequest();
        verify(path, times(1)).value();
        verify(request, times(2)).getCookies();

    }

}
