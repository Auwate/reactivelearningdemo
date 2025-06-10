package com.reactivelearning.demo.dto.auth;

import com.reactivelearning.demo.exception.entities.TOTPInvalidException;
import com.reactivelearning.demo.exception.entities.TOTPNotProvidedException;

/**
 * A DTO class for use with /login. It holds data useful for logging in, such as 2FA (totp codes)
 */
public class LoginRequest {

    private String totp;

    public LoginRequest() {}

    public LoginRequest(String totp) {
        this.totp = totp;
    }

    /**
     * Get the TOTP code
     * - If the TOTP code was not provided, is blank, or length != 6, then it's wrong.
     * @return Integer : TOTP in int format
     */
    public int getTotp() {

        if (totp == null || totp.isBlank()) {
            throw new TOTPNotProvidedException("TOTP not provided");
        } else if (totp.length() != 6) {
            throw new TOTPInvalidException("Provided TOTP is invalid");
        }

        try {
            return Integer.parseInt(totp);
        } catch (NumberFormatException ex) {
            throw new TOTPInvalidException("Provided TOTP is not a number");
        }

    }

    public void setTotp(String totp) {
        this.totp = totp;
    }

}
