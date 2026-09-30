package com.kitchenline.api.engine;

public enum SkillLevel {
    BEGINNER(1, "2.0–2.5"),
    ADVANCED_BEGINNER(2, "3.0"),
    INTERMEDIATE(3, "3.5"),
    ADVANCED(4, "4.0"),
    EXPERT(5, "4.5+");

    private final int value;
    private final String rating;

    SkillLevel(int value, String rating) {
        this.value = value;
        this.rating = rating;
    }

    public int getValue() { return value; }
    public String getRating() { return rating; }
}