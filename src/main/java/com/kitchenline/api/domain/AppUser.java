package com.kitchenline.api.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // Never the real password: a scrambled "hash" of it (set up in step 4)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_status", nullable = false, length = 20)
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.TRIAL;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AppUser() {
        // Required by JPA. Our own code uses the constructor below.
    }

    public AppUser(String email, String passwordHash) {
        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public SubscriptionStatus getSubscriptionStatus() { return subscriptionStatus; }
    public Instant getCreatedAt() { return createdAt; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setSubscriptionStatus(SubscriptionStatus status) { this.subscriptionStatus = status; }
}