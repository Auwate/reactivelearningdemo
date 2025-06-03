package com.reactivelearning.demo.security.jwt;

import com.reactivelearning.demo.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

public class JwtUtil {

    private final String secret;
    private final Duration EXPIRATION_TIMER = Duration.ofMinutes(10);


    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    /**
     * getExpirationTimerInSeconds
     * - We do not need to worry about the expiration timer overflowing as an int, as we would
     * need to have an expiration timer of 60 decades.
     * @return int : Expiration in seconds
     */
    public int getExpirationTimerInSeconds() {
        return (int) EXPIRATION_TIMER.toSeconds();
    }

    private String getSecret() {
        return this.secret;
    }


}
