package com.reactivelearning.demo.repository.user;

import com.reactivelearning.demo.entities.Mfa;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface MfaRepository extends ReactiveCrudRepository<Mfa, UUID> {
    Mono<Mfa> findByUsersId(UUID usersId);
}
