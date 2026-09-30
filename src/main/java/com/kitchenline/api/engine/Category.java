package com.kitchenline.api.engine;

public enum Category {
    LATERAL("Lateral movement"),
    GLUTE_HIP("Glute & hip strength"),
    SHOULDER("Shoulder stability"),
    POWER_CORE("Paddle power & core");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}