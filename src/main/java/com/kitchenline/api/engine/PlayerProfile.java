package com.kitchenline.api.engine;

public record PlayerProfile(
        int age,
        Gender gender,
        SkillLevel skill,
        int drillsPerWeek,
        int playsPerWeek,
        int workoutsPerWeek
) {
    // Compact constructor: runs every time a profile is created and rejects bad input
    public PlayerProfile {
        if (age < 16 || age > 90) throw new IllegalArgumentException("Age must be 16-90");
        if (gender == null) throw new IllegalArgumentException("Gender is required");
        if (skill == null) throw new IllegalArgumentException("Skill level is required");
        if (drillsPerWeek < 0 || drillsPerWeek > 7) throw new IllegalArgumentException("Drills must be 0-7");
        if (playsPerWeek < 0 || playsPerWeek > 7) throw new IllegalArgumentException("Plays must be 0-7");
        if (workoutsPerWeek < 1 || workoutsPerWeek > 6) throw new IllegalArgumentException("Workouts must be 1-6");
    }

    /** Total drill and play sessions per week. */
    public int courtSessions() {
        return drillsPerWeek + playsPerWeek;
    }
}