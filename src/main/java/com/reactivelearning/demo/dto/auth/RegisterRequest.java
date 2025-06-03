package com.reactivelearning.demo.dto.auth;

/**
 * A DTO class for use with /register. It holds all the necessary data to register.
 */
public class RegisterRequest {

    private String username;
    private String password;
    private String email;

    public RegisterRequest(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public RegisterRequest() {}

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

}
