package com.reactivelearning.demo.service;

import com.reactivelearning.demo.dto.PartialUserDTO;
import com.reactivelearning.demo.dto.UserDTO;
import com.reactivelearning.demo.entities.Role;
import com.reactivelearning.demo.entities.SimpleUserDetails;
import com.reactivelearning.demo.entities.User;
import com.reactivelearning.demo.entities.UserRoles;
import com.reactivelearning.demo.exception.entities.ExistsException;
import com.reactivelearning.demo.exception.entities.NotFoundException;
import com.reactivelearning.demo.repository.RolesRepository;
import com.reactivelearning.demo.repository.UsersRolesRepository;
import com.reactivelearning.demo.repository.UsersRepository;
import com.reactivelearning.demo.security.util.PasswordHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class UserService implements ReactiveUserDetailsService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final UsersRolesRepository usersRolesRepository;
    private final PasswordHandler passwordHandler;

    private static final Logger logger = LoggerFactory.getLogger(ReactiveUserDetailsService.class);

    @Autowired
    public UserService(
            UsersRepository usersRepository,
            RolesRepository rolesRepository,
            UsersRolesRepository usersRolesRepository,
            PasswordHandler passwordHandler) {
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.usersRolesRepository = usersRolesRepository;
        this.passwordHandler = passwordHandler;
    }

    public Flux<Map<UUID, UserDTO>> getUsers() {
        return usersRepository.findAll()
                .map(user -> Map.of(
                        user.getId(),
                        UserDTO.of(user.getUsername(), user.getPassword(), user.getEmail())));
    }

    public Mono<Map<UUID, UserDTO>> addUser(UserDTO user) {
        return usersRepository.findByUsername(user.getUsername())
                .flatMap(exists -> Mono.error(new ExistsException("Username exists.")))
                .switchIfEmpty(usersRepository.save(dtoToUser(user))
                        .flatMap(savedUser -> rolesRepository.findByRole("USER")
                                .switchIfEmpty(rolesRepository.save(Role.of("USER")))
                                .flatMap(savedRole -> usersRolesRepository.save(
                                        UserRoles.of(
                                                savedUser.getId(),
                                                savedRole.getId()))
                                        .thenReturn(savedUser))))
                .map(targetUser -> {
                    User savedUser = (User)targetUser;
                    return Map.of(
                            savedUser.getId(),
                            UserDTO.of(
                                    savedUser.getUsername(),
                                    savedUser.getPassword(),
                                    savedUser.getEmail()));
                });

    }

    public Mono<Void> deleteUser(UUID id) {
        return usersRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Not Found.")))
                .flatMap(savedUser -> usersRepository.delete(savedUser));
    }

    public Mono<UserDTO> updateUser(UUID id, PartialUserDTO user) {
        return usersRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Not Found")))
                .map(existingUser -> existingUser.update(user.getUsername(), user.getPassword(), user.getEmail()))
                .flatMap(existingUser -> usersRepository.save(existingUser))
                .map(existingUser -> UserDTO.of(existingUser.getUsername(), existingUser.getPassword(), existingUser.getEmail()));
    }

    // Overrides

    @Override
    public Mono<UserDetails> findByUsername(String username) throws UsernameNotFoundException {
        return usersRepository.findByUsername(username)
                .switchIfEmpty(Mono.just(User.of()))
                .map(user -> SimpleUserDetails.of(user));
    }

    // Private methods

    private User dtoToUser(UserDTO userDTO) {
        return hashUserPassword(User.of(userDTO));
    }

    private User hashUserPassword(User user) {
        user.setPassword(passwordHandler.hash(user.getPassword()));
        return user;
    }

}
