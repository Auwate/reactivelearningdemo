package com.reactivelearning.demo.repository.user;

import com.reactivelearning.demo.entities.UserRoles;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UsersRolesRepository extends ReactiveCrudRepository<UserRoles, UUID> {
    @Query(
            """
            SELECT *
            FROM users_roles ur
            WHERE ur.users_id = :usersId AND ur.roles_id = :rolesId
            """
    )
    Mono<UserRoles> findByUsersIdAndRolesId(UUID usersId, UUID rolesId);
}
