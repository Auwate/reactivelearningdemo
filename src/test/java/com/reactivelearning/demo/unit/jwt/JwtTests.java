package com.reactivelearning.demo.unit.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.reactivelearning.demo.entities.Role;
import com.reactivelearning.demo.entities.User;
import com.reactivelearning.demo.security.jwt.JwtUtil;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTests {

    private JwtUtil jwtUtil;
    private final String SECRET = "cCelTkojj7";

    public JwtTests() {
        this.jwtUtil = new JwtUtil(SECRET);
    }

    /**
     * Given valid data, a valid JWT should be created
     */
    @Test
    void shouldCreateValidJwtGivenValidData() {

        User user = new User("Test", "Test", "Test");
        user.setRoles(List.of(new Role("USER")));
        user.setId(UUID.randomUUID());

        String jwt = jwtUtil.generateToken(user);

        assertTrue(jwtUtil.isValid(jwt));

    }

    /**
     * Given a valid JWT, isValid should return True
     */
    @Test
    void shouldReturnTrueOnValidData() {

        Algorithm algorithm = Algorithm.HMAC512(SECRET);

        String validJwt = JWT.create()
                .withExpiresAt(
                        new Date(
                                System.currentTimeMillis() + Duration.ofMinutes(10).toMillis()))
                .sign(algorithm);

        assertTrue(jwtUtil.isValid(validJwt));

    }

    /**
     * Given an invalid JWT, isValid should return False. Specifically, this Jwt was not signed
     * with the same secret ket
     */
    @Test
    void shouldReturnFalseOnInvalidJwt() {

        Algorithm algorithm = Algorithm.HMAC512("NOTVALIDKEY");

        String invalidJwt = JWT.create()
                .withExpiresAt(
                        new Date( // Valid Date
                                System.currentTimeMillis() + Duration.ofMinutes(10).toMillis()))
                .sign(algorithm);

        assertFalse(jwtUtil.isValid(invalidJwt));

    }

    /**
     * Given an expired JWT that was correctly signed, isValid should return false.
     */
    @Test
    void shouldReturnFalseOnExpiredJwt() {

        Algorithm algorithm = Algorithm.HMAC512(SECRET);

        String expiredJwt = JWT.create()
                .withExpiresAt(new Date())
                .sign(algorithm);

        assertFalse(jwtUtil.isValid(expiredJwt));

    }

}
