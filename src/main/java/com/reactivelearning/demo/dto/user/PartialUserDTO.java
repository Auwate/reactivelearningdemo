package com.reactivelearning.demo.dto.user;

public class PartialUserDTO {

    private String username;
    private String password;
    private String email;

    public PartialUserDTO(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public PartialUserDTO() {}

    public static PartialUserDTO of(String username, String password, String email) {
        return new PartialUserDTO(username, password, email);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
