package com.reactivelearning.demo.entities;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("users_roles")
public class UserRoles {

    @Column("users_id")
    private UUID usersId;

    @Column("roles_id")
    private UUID rolesId;

    public UserRoles() {}

    public UserRoles(UUID usersId, UUID rolesId) {
        this.usersId = usersId;
        this.rolesId = rolesId;
    }

    public static UserRoles of(UUID usersId, UUID rolesId) {
        return new UserRoles(usersId, rolesId);
    }

    public UUID getUsersId() {
        return usersId;
    }

    public void setUsersId(UUID usersId) {
        this.usersId = usersId;
    }

    public UUID getRolesId() {
        return rolesId;
    }

    public void setRolesId(UUID rolesId) {
        this.rolesId = rolesId;
    }

}
