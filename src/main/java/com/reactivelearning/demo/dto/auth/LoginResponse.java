package com.reactivelearning.demo.dto.auth;

/**
 * A DTO class that the API returns when hitting /login. It simplifies the controller logic by allowing the
 * service layer to specify if the server succeeded, needs more information, or authentication failed.
 */
public class LoginResponse {

    private boolean success;
    private boolean requires2fa;
    private boolean invalid2fa;
    private String jwtToken;

    public LoginResponse() {}

    public LoginResponse(
            boolean success,
            boolean requires2fa,
            boolean invalid2fa,
            String jwtToken) {
        this.success = success;
        this.requires2fa = requires2fa;
        this.invalid2fa = invalid2fa;
        this.jwtToken = jwtToken;
    }

    public static LoginResponse of(
            boolean success,
            boolean requires2fa,
            boolean invalid2fa,
            String jwtToken) {
        return new LoginResponse(success, requires2fa, invalid2fa, jwtToken);
    }

    public boolean success() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean requires2fa() {
        return requires2fa;
    }

    public void setRequires2fa(boolean requires2fa) {
        this.requires2fa = requires2fa;
    }

    public boolean invalid2fa() {
        return invalid2fa;
    }

    public void setInvalid2fa(boolean invalid2fa) {
        this.invalid2fa = invalid2fa;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void setJwtToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

}
