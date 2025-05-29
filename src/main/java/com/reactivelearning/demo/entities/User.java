package com.reactivelearning.demo.entities;

import com.reactivelearning.demo.dto.UserDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("users")
public class User {

    @Id
    private UUID id;

    private String username;
    private String password;
    private String email;

    public User (String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User () {}

    public static User of(UserDTO user) {
        return new User(user.getUsername(), user.getPassword(), user.getEmail());
    }

    public static User of() {
        return new User("N/A", "N/A", "N/A");
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User update(String username, String password, String email) {
        if (username != null) {
            this.setUsername(username);
        } if (password != null) {
            this.setPassword(password);
        } if (email != null) {
            this.setEmail(email);
        }
        return this;
    }

}
