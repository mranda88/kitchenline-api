package com.kitchenline.api.controller;

import com.kitchenline.api.engine.Gender;
import com.kitchenline.api.engine.PlayerProfile;
import com.kitchenline.api.engine.SkillLevel;

public record ProfileRequest(
        Integer age,
        Gender gender,
        SkillLevel skill,
        Integer drillsPerWeek,
        Integer playsPerWeek,
        Integer workoutsPerWeek
) {
    /** Checks nothing is missing, then builds the engine's PlayerProfile (which checks the ranges). */
    public PlayerProfile toPlayerProfile() {
        if (age == null || gender == null || skill == null
                || drillsPerWeek == null || playsPerWeek == null || workoutsPerWeek == null) {
            throw new IllegalArgumentException("All six profile answers are required.");
        }
        return new PlayerProfile(age, gender, skill, drillsPerWeek, playsPerWeek, workoutsPerWeek);
    }
}