package com.kitchenline.api.engine;

import java.util.List;

/** One workout: Day A, Day B, ... */
public record WorkoutDay(
        String label,
        Category focus,
        List<WorkoutBlock> blocks
) {}