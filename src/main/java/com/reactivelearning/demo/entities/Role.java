package com.reactivelearning.demo.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * An entity class for use with the database. It holds the fields necessary to populate the roles table, and has
 * a many-to-many connection with Users.
 */
@Table("roles")
public class Role {

    @Id
    private UUID id;
    private String role;

    public Role() {}

    public Role(String role) {
        this.role = role;
    }

    public static Role of(String role) {
        return new Role(role);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

}
