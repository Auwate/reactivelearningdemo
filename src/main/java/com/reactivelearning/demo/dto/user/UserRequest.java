package com.reactivelearning.demo.dto.user;

import com.reactivelearning.demo.dto.auth.RegisterRequest;
import com.reactivelearning.demo.entities.RoleType;

/**
 * A DTO class for use when making a request to create a User. It holds everything a potential user will need.
 */
public class UserRequest {

    private String username;
    private String password;
    private String email;
    private RoleType role;

    public UserRequest() {}

    public UserRequest(String username, String password, String email, RoleType role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public static UserRequest fromRegisterRequest(RegisterRequest registerRequest, RoleType role) {
        return new UserRequest(
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getEmail(),
                role);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public RoleType getRole() {
        return role;
    }

}
