package com.reactivelearning.demo.service;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.RegistryConfiguration;
import com.reactivelearning.demo.entities.Mfa;
import com.reactivelearning.demo.entities.User;
import com.reactivelearning.demo.exception.entities.InternalServerException;
import com.reactivelearning.demo.exception.entities.MfaNotFoundException;
import com.reactivelearning.demo.exception.entities.MfaRepositoryException;
import com.reactivelearning.demo.repository.user.MfaRepository;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 * A service class for working with Mfa objects.
 */
@Service
public class MfaService {

    private final KeysetHandle keysetHandle;
    private final MfaRepository mfaRepository;
    private final GoogleAuthenticator authenticator;

    private final Logger logger = LoggerFactory.getLogger(MfaService.class);

    public MfaService(
            KeysetHandle keysetHandle,
            MfaRepository mfaRepository,
            GoogleAuthenticator authenticator) {
        this.keysetHandle = keysetHandle;
        this.mfaRepository = mfaRepository;
        this.authenticator = authenticator;
    }

    // Asynchronous - Database methods

    /**
     * Create MFA
     * - Creates an MFA row in the MFA table, using a User's UUID
     * @param user User : The registering user
     * @return Mfa : A created Mfa entity
     */
    public Mono<Mfa> createMfa(User user) {
        return Mono.fromSupplier(() -> Mfa.of(user.getId(), false, createSecureKey()))
                .flatMap(mfa ->
                        Mono.fromSupplier(() -> {
                            mfa.setMfaSecret(encrypt(mfa.getMfaSecret()));
                            return mfa;
                        })) // Possible error.
                .doOnError(exception -> logger.error("Failed to encrypt: {}", exception.getMessage()))
                .flatMap(mfa -> mfaRepository.save(mfa))
                .doOnError(exception -> {
                    logger.error("Failed to save MFA: {}", exception.getMessage());
                    throw new MfaRepositoryException(exception.getMessage());
                });
    }

    // Asynchronous - Private

    /**
     * Get a User's MFA data
     * - Uses the User's UUID to find the data
     * @param user User : The linked User
     * @return Mfa : The Mfa data
     */
    public Mono<Mfa> getMfa(User user) {
        return mfaRepository.findByUsersId(user.getId())
                .switchIfEmpty(Mono.error(new MfaNotFoundException("MFA data not found.")))
                .flatMap(mfa -> Mono.fromSupplier(() -> {
                    mfa.setMfaSecret(decrypt(mfa.getMfaSecret()));
                    return mfa;
                }))
                .doOnError(exception -> {
                    logger.error("Failed to decrypt: {}", exception.getMessage());
                    throw new MfaRepositoryException(exception.getMessage());
                });
    }

    // Synchronous - Encryption methods

    // Public

    /**
     * Validate
     * - A synchronous, blocking request to check if the code is valid
     * @param mfa Mfa : A user's MFA data
     * @param code int : The TOTP code provided
     * @return boolean : Valid or not
     */
    public boolean validate(Mfa mfa, int code) {
        return authenticator.authorize(mfa.getMfaSecret(), code);
    }

    // Private

    /**
     * Create Secure Key
     * - Uses GoogleAuthenticator to create a randomly generated key
     * @return String : Random key for use in TOTP
     */
    private String createSecureKey() {
        return authenticator.createCredentials().getKey();
    }

    /**
     * Encrypt
     * - Use AEAD encryption with the KeysetHandler (holds our MFA Secret key)
     * @param raw String : The raw string to be encrypted
     * @return String : Encrypted string
     */
    private String encrypt(String raw) {
        try {
            Aead aead = keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead.class);
            byte[] cipherText = aead.encrypt(raw.getBytes(StandardCharsets.UTF_8), "Reactive AuthN/AuthZ".getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (GeneralSecurityException exception) {
            throw new InternalServerException(String.format("Encryption failed: %s", exception.getMessage()));
        }
    }

    /**
     * Decrypt
     * - Use AEAD decryption with the KeysetHandler (holds the MFA Secret key)
     * @param encrypted String : The encrypted string to be decrypted
     * @return String : Decrypted raw string
     */
    private String decrypt(String encrypted) {
        try {
            Aead aead = keysetHandle.getPrimitive(RegistryConfiguration.get(), Aead.class);
            byte[] rawText = aead.decrypt(
                    Base64.getDecoder().decode(encrypted),
                    "Reactive AuthN/AuthZ".getBytes()
            );
            return new String(rawText, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException exception) {
            throw new InternalServerException(String.format("Encryption failed: %s", exception.getMessage()));
        }
    }

}
