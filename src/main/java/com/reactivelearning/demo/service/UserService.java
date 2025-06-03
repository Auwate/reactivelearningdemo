package com.reactivelearning.demo.service;

import com.reactivelearning.demo.dto.auth.LoginRequest;
import com.reactivelearning.demo.dto.auth.LoginResponse;
import com.reactivelearning.demo.dto.auth.RegisterRequest;
import com.reactivelearning.demo.dto.auth.RegisterResponse;
import com.reactivelearning.demo.dto.user.PartialUserDTO;
import com.reactivelearning.demo.dto.user.UserDTO;
import com.reactivelearning.demo.dto.user.UserRequest;
import com.reactivelearning.demo.entities.*;
import com.reactivelearning.demo.exception.entities.ExistsException;
import com.reactivelearning.demo.exception.entities.NotFoundException;
import com.reactivelearning.demo.exception.entities.RolesNotFoundException;
import com.reactivelearning.demo.repository.RolesRepository;
import com.reactivelearning.demo.repository.UsersRolesRepository;
import com.reactivelearning.demo.repository.UsersRepository;
import com.reactivelearning.demo.security.util.PasswordHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService implements ReactiveUserDetailsService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final UsersRolesRepository usersRolesRepository;
    private final PasswordHandler passwordHandler;
    private final TransactionalOperator transactionalOperator;

    private static final Logger logger = LoggerFactory.getLogger(ReactiveUserDetailsService.class);

    @Autowired
    public UserService(
            UsersRepository usersRepository,
            RolesRepository rolesRepository,
            UsersRolesRepository usersRolesRepository,
            PasswordHandler passwordHandler,
            TransactionalOperator transactionalOperator) {
        this.usersRepository = usersRepository;
        this.rolesRepository = rolesRepository;
        this.usersRolesRepository = usersRolesRepository;
        this.passwordHandler = passwordHandler;
        this.transactionalOperator = transactionalOperator;
    }

    /**
     * Login
     * - The service layer implementation of a login process
     * - Part of the process is already completed for us via Spring Security with
     * ReactiveAuthenticationManager.
     * @param loginRequest Object of LoginRequest, holds the login data
     * @return LoginResponse : The result of logging in
     */
    public Mono<LoginResponse> login(LoginRequest loginRequest) {
        return ReactiveSecurityContextHolder.getContext()
                .map(context -> (User) context.getAuthentication().getPrincipal())
                .map(secureUser -> LoginResponse.of(true, false, false, ""));
    }

    /**
     * Register
     * - The service layer implementation of the register process.
     * - Stays lean by offloading creation to a CRUD operation, passing necessary parameters.
     * @param registerRequest Object of RegisterRequest, holds the register data
     * @return RegisterResponse : The result of trying to register under these credentials
     */
    public Mono<RegisterResponse> register(RegisterRequest registerRequest) {
        return createUser(UserRequest.fromRegisterRequest(registerRequest, RoleType.USER))
                .map(savedUser -> RegisterResponse.fromUser(savedUser))
                .doOnNext(sub -> logger.info("Successfully registered user {}", registerRequest.getUsername()));
    }

    // CRUD operations

    /**
     * Create User - CRUD Operation
     * - The dedicated method to creating a user and their required dependencies
     * @param userRequest Object of UserRequest, holds data for the potential user
     * @return User : The created user
     */
    public Mono<User> createUser(UserRequest userRequest) {
        return getRole(userRequest.getRole())
                .flatMap(foundRole ->
                        ensureUserDoesNotExist(userRequest.getUsername())
                                .then(Mono.from(
                                        transactionalOperator.transactional( // ATOMIC
                                                saveUserFromRequest(userRequest)
                                                        .flatMap(savedUser -> saveUserRole(savedUser, foundRole)
                                                                .thenReturn(savedUser)))))
                                .map(savedUser -> {
                                    savedUser.setRoles(List.of(foundRole));
                                    return savedUser;
                                }))
                .doOnError(error -> logger.error("Error when creating user {}", userRequest.getUsername()));

    }

    // Getters

    public Mono<Role> getRole(RoleType role) {
        return rolesRepository.findByRole(role.name())
                .switchIfEmpty(Mono.error(new RolesNotFoundException(
                        "The server experienced an issue.")));
    }

    public Mono<User> getUser(String username) {
        return usersRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new NotFoundException(
                        "User not found with credentials.")));
    }

    // Setters

    public Mono<UserRoles> saveUserRole(User user, Role role) {
        return Mono.defer(() ->
                usersRolesRepository.save(UserRoles.of(user.getId(), role.getId())))
                .switchIfEmpty(Mono.error(new Exception("Could not save user and role IDs.")));
    }

    public Mono<User> saveUserFromRequest(UserRequest userRequest) {
        return Mono.defer(() -> usersRepository.save(dtoToUser(userRequest))
                .switchIfEmpty(Mono.error(new ExistsException("The user could not be saved."))));
    }

    // Exists

    public Mono<Void> ensureUserDoesNotExist(String username) {
        return usersRepository.findByUsername(username)
                .flatMap(exists -> Mono.error(new ExistsException("Username is taken.")))
                .then(Mono.empty());
    }

    public Flux<Map<UUID, UserDTO>> getUsers() {
        return usersRepository.findAll()
                .map(user -> Map.of(
                        user.getId(),
                        UserDTO.of(user.getUsername(), user.getPassword(), user.getEmail())));
    }

    public Mono<Map<UUID, UserDTO>> addUser(UserDTO user) {
        return Mono.defer(() -> usersRepository.findByUsername(user.getUsername()))
                .flatMap(exists -> Mono.error(new ExistsException("Username exists.")))
                .switchIfEmpty(Mono.defer(() -> usersRepository.save(dtoToUser(user)))
                        .flatMap(savedUser -> rolesRepository.findByRole("USER")
                                .switchIfEmpty(Mono.defer(() -> rolesRepository.save(Role.of("USER"))))
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

    /**
     * FindByUsername
     * - Used by ReactiveAuthenticationManager to pull users by their username
     * @param username String : The username provided by the user in the Authorization header
     * @return User (Implements UserDetails) : An object of User that allows ReactiveAuthenticationManager to
     * authenticate
     * @throws UsernameNotFoundException : If the username does not exist in the database.
     * @throws RolesNotFoundException : If the user does not have a Role
     */
    @Override
    public Mono<UserDetails> findByUsername(String username) throws UsernameNotFoundException {
        return usersRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Username not found.")))
                .flatMap(user -> Mono.defer(() -> rolesRepository.findByUserId(user.getId())
                            .switchIfEmpty(Mono.error(new RolesNotFoundException(
                                    "The server had a problem finding the user's configurations."
                            )))
                            .collectList()
                            .map(roles -> {
                                user.setRoles(roles);
                                return user;
                            })));
    }

    // Private methods

    private User dtoToUser(UserDTO userDTO) {
        return hashUserPassword(User.fromDTO(userDTO));
    }

    private User dtoToUser(UserRequest userRequest) {
        return hashUserPassword(User.fromRequest(userRequest));
    }

    private User hashUserPassword(User user) {
        user.setPassword(passwordHandler.hash(user.getPassword()));
        return user;
    }

}
