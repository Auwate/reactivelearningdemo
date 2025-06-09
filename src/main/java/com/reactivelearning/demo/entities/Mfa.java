package com.reactivelearning.demo.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "mfa")
public class Mfa {

    @Id
    private UUID id;

    @Column("users_id")
    private UUID usersId;

    @Column("mfa_secret")
    private String mfaSecret;

    private boolean enabled;

    public Mfa() {}

    public Mfa(UUID usersId, boolean enabled, String mfaSecret) {
        this.usersId = usersId;
        this.enabled = enabled;
        this.mfaSecret = mfaSecret;
    }

    public static Mfa of(UUID usersId, boolean enabled, String encryptedMfaSecret) {
        return new Mfa(usersId, enabled, encryptedMfaSecret);
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public UUID getUsersId() {
        return usersId;
    }

    public String getMfaSecret() {
        return mfaSecret;
    }

    public void setMfaSecret(String mfaSecret) {
        this.mfaSecret = mfaSecret;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
