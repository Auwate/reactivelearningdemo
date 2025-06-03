package com.reactivelearning.demo.entities;

/**
 * A simple Enum for easily distinguishing between which Role we would like to attach to a user.
 */
public enum RoleType {

    USER,
    ADMIN;

    public String getAuthority() {
        return "ROLE_" + this.name();
    }

}
