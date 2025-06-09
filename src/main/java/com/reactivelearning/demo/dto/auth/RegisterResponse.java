package com.reactivelearning.demo.dto.auth;

import com.reactivelearning.demo.entities.User;

/**
 * A DTO class for using at /register. It holds necessary data to respond from the controller.
 */
public class RegisterResponse {

    private boolean success;
    private String jwtToken;
    private String mfaUri;

    public RegisterResponse() {}

    public RegisterResponse(boolean success, String jwtToken, String mfaUri) {
        this.success = success;
        this.jwtToken = jwtToken;
        this.mfaUri = mfaUri;
    }

    public static RegisterResponse of(User user, String uri) {
        return new RegisterResponse(true, "", uri);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public String getMfaUri() {
        return mfaUri;
    }

}
