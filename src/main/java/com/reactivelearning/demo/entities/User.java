package com.reactivelearning.demo.entities;

import com.reactivelearning.demo.dto.user.UserDTO;
import com.reactivelearning.demo.dto.user.UserRequest;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * An entity class for use with the database. It holds various database fields, and has a many-to-many connection
 * with Roles.
 */
@Table("users")
public class User implements UserDetails {

    @Id
    private UUID id;

    private String username;
    private String password;
    private String email;

    @Transient
    private List<Role> roles;

    @Transient
    private Mfa mfa;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(String username, String password, String email, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = List.of(role);
    }

    public User() {}

    public static User fromDTO(UserDTO user) {
        return new User(user.getUsername(), user.getPassword(), user.getEmail());
    }

    public static User fromRequest(UserRequest user) {
        return new User(user.getUsername(), user.getPassword(), user.getEmail(), Role.of(user.getRole().name()));
    }

    public static User of() {
        return new User("N/A", "N/A", "N/A");
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRole()))
                .toList();
    }

    public List<String> getRolesAsStrings() {
        return roles.stream()
                .map(role -> "ROLE_{}" + role.getRole())
                .toList();
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

    public List<Role> getRoles() {return this.roles;}

    public void setRoles(List<Role> roles) {this.roles = roles;}

    public Mfa getMfa() {
        return this.mfa;
    }

    public void setMfa(Mfa mfa) {
        this.mfa = mfa;
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
