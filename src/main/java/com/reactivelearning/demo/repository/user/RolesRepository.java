package com.reactivelearning.demo.repository.user;

import com.reactivelearning.demo.entities.Role;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface RolesRepository extends ReactiveCrudRepository<Role, UUID> {
    Mono<Role> findByRole(String roleName);

    @Query("""
    SELECT r.id, r.role
    FROM roles r
    JOIN users_roles ur ON ur.roles_id = r.id
    WHERE ur.users_id = :userId
    """)
    Flux<Role> findByUserId(UUID userId);
}