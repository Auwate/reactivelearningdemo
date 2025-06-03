package com.reactivelearning.demo.dto.auth;

import com.reactivelearning.demo.entities.User;

/**
 * A DTO class for using at /register. It holds necessary data to respond from the controller.
 */
public class RegisterResponse {

    private boolean success;
    private String jwtToken;


    public RegisterResponse() {}

    public RegisterResponse(boolean success, String jwtToken) {
        this.success = success;
        this.jwtToken = jwtToken;
    }

    public static RegisterResponse fromUser(User user) {
        return new RegisterResponse(true, "");
    }

    public boolean isSuccess() {
        return success;
    }

    public String getJwtToken() {
        return jwtToken;
    }

}
