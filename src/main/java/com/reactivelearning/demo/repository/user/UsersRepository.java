package com.reactivelearning.demo.repository.user;

import com.reactivelearning.demo.entities.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UsersRepository extends ReactiveCrudRepository<User, UUID> {
    Mono<User> findByUsername(String username);
}
