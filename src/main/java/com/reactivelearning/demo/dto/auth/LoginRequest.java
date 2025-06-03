package com.reactivelearning.demo.dto.auth;

/**
 * A DTO class for use with /login. It holds data useful for logging in, such as 2FA (totp codes)
 */
public class LoginRequest {

    private String totp;

    public LoginRequest() {}

    public LoginRequest(String totp) {
        this.totp = totp;
    }

    public String getTotp() {
        return totp;
    }

    public void setTotp(String totp) {
        this.totp = totp;
    }

}
