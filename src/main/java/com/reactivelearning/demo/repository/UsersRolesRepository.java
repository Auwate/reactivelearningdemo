package com.reactivelearning.demo.repository;

import com.reactivelearning.demo.entities.UserRoles;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UsersRolesRepository extends ReactiveCrudRepository<UserRoles, Void> {
    Mono<UserRoles> findByUsersIdAndRolesId(UUID usersId, UUID rolesId);
    Mono<Void> deleteByUsersIdAndRolesId(UUID usersId, UUID rolesId);
    Mono<UserRoles> findByUsersId(UUID usersId);
}
