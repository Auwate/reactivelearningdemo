package com.reactivelearning.demo.security.util;

import com.reactivelearning.demo.exception.entities.WeakPasswordException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHandler {

    private static final int MIN_PASSWORD_LEN = 8;

    private final PasswordEncoder encoder;

    @Autowired
    public PasswordHandler(PasswordEncoder encoder) {
        this.encoder = encoder;
    }

    public String hash(String raw) {

        if (raw.length() < MIN_PASSWORD_LEN) {
            throw new WeakPasswordException("Too short.");
        }

        return encoder.encode(raw);

    }

    public boolean compare(String raw, String hashed) {
        return encoder.matches(raw, hashed);
    }

}
