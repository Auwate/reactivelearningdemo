package com.reactivelearning.demo.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.reactivelearning.demo.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtUtil {

    private final String secret;

    private final Duration EXPIRATION_TIMER = Duration.ofDays(1);

    private final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Autowired
    public JwtUtil(
            @Value("${jwt.secret}") String secret) {
        this.secret = getSecret(secret);
    }

    /**
     * generateToken
     * - Public method to create a JWT
     * @param user User : The user to create a JWT
     * @return String : The JWT in string format
     */
    public String generateToken(User user) {
        return createJwtToken(user, Algorithm.HMAC512(getSecret()));
    }

    /**
     * createJwtToken
     * - Private method to create a JWT
     * @param user User : User to create a Jwt for
     * @param algorithm Algorithm: The algorithm to sign the JWT. Holds the secret key as well
     * @return String : JWT in String format
     */
    private String createJwtToken(User user, Algorithm algorithm) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withClaim("authorities", user.getRoles().stream().toList())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + getExpirationTimerInMillis()))
                .sign(algorithm);
    }

    /**
     * getSecret
     * - Get the secret key, but only allow within this class.
     * @return String : Secret key
     */
    private String getSecret() {
        return this.secret;
    }

    /**
     * getExpirationTimerInSeconds
     * - We do not need to worry about the expiration timer overflowing as an int, as we would
     * need to have an expiration timer of 60 decades.
     * @return int : Expiration in seconds
     */
    private int getExpirationTimerInSeconds() {
        return (int) EXPIRATION_TIMER.toSeconds();
    }

    /**
     * getExpirationTimerInMillis
     * - Used by the creation method to get the expiration timer in milliseconds
     * @return long : Expiration in milliseconds
     */
    private long getExpirationTimerInMillis() {
        return EXPIRATION_TIMER.toMillis();
    }

    /**
     * getSecret
     * - Given a secret key, return the value or create a random one if secretKey == 'Undefined'
     * @param secretKey String : The secret key
     * @return String : The secret key OR a random key
     */
    private String getSecret(String secretKey) {
        if (secretKey.equals("Undefined")) {
            SecureRandom secureRandom = new SecureRandom();
            byte[] bytes = new byte[32];
            secureRandom.nextBytes(bytes);
            String key = Arrays.toString(bytes);

            logger.warn("⚠️ Generated ephemeral JWT secret key: {}", key);
            return key;
        } else {
            return secretKey;
        }
    }

}
