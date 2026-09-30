package com.kitchenline.api.domain;

import com.kitchenline.api.engine.PlayerProfile;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "player_profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each user has exactly one profile
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Embedded
    private ProfileData data;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Profile() {
        // Required by JPA
    }

    public Profile(AppUser user, PlayerProfile answers) {
        this.user = user;
        this.data = ProfileData.from(answers);
    }

    /** Called when the player edits their answers. */
    public void update(PlayerProfile answers) {
        this.data = ProfileData.from(answers);
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public PlayerProfile toPlayerProfile() { return data.toPlayerProfile(); }
    public Instant getUpdatedAt() { return updatedAt; }
}