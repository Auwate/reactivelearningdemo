package com.reactivelearning.demo.entities;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SimpleUserDetails implements UserDetails {

    private String username;
    private String password;

    public SimpleUserDetails() {}

    public SimpleUserDetails(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static SimpleUserDetails of(User user) {
        return new SimpleUserDetails(user.getUsername(), user.getPassword());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

}
