package com.kitchenline.api.engine;

import org.springframework.stereotype.Component;

@Component
public class TierCalculator {

    private static final String[] LABELS =
            {"Foundation", "Build", "Compete", "Performance", "Elite"};

    /**
     * Works out the player's tier for a given month of their subscription.
     * Month 1 = their starting tier. Every 2 months they step up one tier,
     * but never past the ceiling for their age.
     */
    public Intensity calculate(PlayerProfile profile, int month) {
        if (month < 1) throw new IllegalArgumentException("Month starts at 1");

        int cap = ageCap(profile.age());

        // Skill (1-5) scaled by age, rounded, kept between 1 and 5
        double raw = profile.skill().getValue() * ageFactor(profile.age());
        int baseTier = clamp((int) Math.round(raw), 1, 5);
        baseTier = Math.min(baseTier, cap);

        // Month-over-month progression: +1 tier every 2 months
        int tier = Math.min(cap, baseTier + (month - 1) / 2);

        boolean jumps = tier >= 4 && profile.age() < 55;

        return new Intensity(tier, LABELS[tier - 1], jumps);
    }

    /** Younger players get a boost; older players are scaled down. */
    double ageFactor(int age) {
        if (age < 30) return 1.1;
        if (age < 40) return 1.0;
        if (age < 50) return 0.9;
        if (age < 60) return 0.8;
        if (age < 70) return 0.7;
        return 0.6;
    }

    /** Highest tier allowed at each age, regardless of skill. */
    int ageCap(int age) {
        if (age >= 70) return 3;
        if (age >= 60) return 4;
        return 5;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}