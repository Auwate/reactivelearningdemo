package com.reactivelearning.demo.security.config;

import com.google.crypto.tink.*;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.PredefinedAeadParameters;
import com.google.crypto.tink.proto.AesGcmKeyFormat;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;

@Configuration
public class MfaConfig {

    private final Logger logger = LoggerFactory.getLogger(MfaConfig.class);

    /**
     * KeysetHandle Bean
     * - Creates a key manager that holds our secret key for encrypting/decrypting MFA secret keys
     * @param secret String : A secret key loaded from the environment, or defaults to Undefined
     * @return KeysetHandle
     * @throws GeneralSecurityException Exception thrown on security-related issues
     * @throws IOException Exception thrown when reading from I/O causes an issue
     */
    @Bean
    public KeysetHandle keysetHandle(@Value("${mfa.secret}") String secret) throws GeneralSecurityException, IOException {

        AeadConfig.register();

        if (!secret.equals("Undefined")) {
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            return CleartextKeysetHandle.read(BinaryKeysetReader.withBytes(keyBytes));
        } else {
            KeysetHandle generated = KeysetHandle.newBuilder()
                    .addEntry(KeysetHandle.generateEntryFromParameters(PredefinedAeadParameters.AES256_GCM)
                            .withRandomId()
                            .makePrimary())
                    .build();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            CleartextKeysetHandle.write(generated, BinaryKeysetWriter.withOutputStream(out));

            String encoded = Base64.getEncoder().encodeToString(out.toByteArray());
            logger.warn("⚠️ Generated ephemeral MFA secret key: {}", encoded);

            return generated;

        }
    }

    @Bean
    public GoogleAuthenticator googleAuthenticator() {
        return new GoogleAuthenticator();
    }

}
