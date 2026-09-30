package com.kitchenline.api.domain;

import com.kitchenline.api.engine.PlayerProfile;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "program_months",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "month_number"}))
public class ProgramMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many months belong to one user
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(name = "month_number", nullable = false)
    private int monthNumber;

    // Frozen copy of the profile when this month started,
    // so editing your profile mid-month doesn't reshuffle the current program
    @Embedded
    private ProfileData profileSnapshot;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    protected ProgramMonth() {
        // Required by JPA
    }

    public ProgramMonth(AppUser user, int monthNumber, PlayerProfile profile) {
        if (monthNumber < 1) throw new IllegalArgumentException("Month starts at 1");
        this.user = user;
        this.monthNumber = monthNumber;
        this.profileSnapshot = ProfileData.from(profile);
    }

    @PrePersist
    void onCreate() {
        startedAt = Instant.now();
    }

    /** Only used before any workout is logged, e.g. fixing a typo in the profile. */
    public void replaceSnapshot(PlayerProfile profile) {
        this.profileSnapshot = ProfileData.from(profile);
    }

    public Long getId() { return id; }
    public AppUser getUser() { return user; }
    public int getMonthNumber() { return monthNumber; }
    public PlayerProfile getProfileSnapshot() { return profileSnapshot.toPlayerProfile(); }
    public Instant getStartedAt() { return startedAt; }
}