package com.kitchenline.api.domain;

import com.kitchenline.api.engine.Gender;
import com.kitchenline.api.engine.PlayerProfile;
import com.kitchenline.api.engine.SkillLevel;
import jakarta.persistence.*;

/**
 * The six profile answers, stored as columns inside another table.
 * Used by Profile (the player's current answers) and ProgramMonth (a frozen copy).
 */
@Embeddable
public class ProfileData {

    @Column(nullable = false)
    private int age;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SkillLevel skill;

    @Column(name = "drills_per_week", nullable = false)
    private int drillsPerWeek;

    @Column(name = "plays_per_week", nullable = false)
    private int playsPerWeek;

    @Column(name = "workouts_per_week", nullable = false)
    private int workoutsPerWeek;

    protected ProfileData() {
        // Required by JPA
    }

    /** Copy an engine PlayerProfile into database form. */
    public static ProfileData from(PlayerProfile p) {
        ProfileData d = new ProfileData();
        d.age = p.age();
        d.gender = p.gender();
        d.skill = p.skill();
        d.drillsPerWeek = p.drillsPerWeek();
        d.playsPerWeek = p.playsPerWeek();
        d.workoutsPerWeek = p.workoutsPerWeek();
        return d;
    }

    /** Turn the stored columns back into an engine PlayerProfile. */
    public PlayerProfile toPlayerProfile() {
        return new PlayerProfile(age, gender, skill, drillsPerWeek, playsPerWeek, workoutsPerWeek);
    }
}