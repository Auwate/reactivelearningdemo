package com.reactivelearning.demo.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Data transfer object, used for creating/getting users
 */
public class UserDTO {

    public UserDTO() {}

    public UserDTO(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public static UserDTO of(String username, String password, String email) {
        return new UserDTO(username, password, email);
    }

    @NotBlank(message = "Username cannot be empty")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    @NotBlank(message = "Email cannot be empty")
    private String email;

    // Getters

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    // Setters

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
