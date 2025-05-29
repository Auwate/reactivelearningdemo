package com.reactivelearning.demo.repository;

import com.reactivelearning.demo.entities.Role;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface RolesRepository extends ReactiveCrudRepository<Role, UUID> {
    Mono<Role> findByRole(String roleName);
}