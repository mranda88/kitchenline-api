package com.kitchenline.api.engine;

import java.util.List;

/** One week of the month, with its overload instructions. */
public record TrainingWeek(
        int number,               // 1-4
        String title,             // "Groove", "Volume", "Sets", "Load"
        String instructions,
        List<WorkoutDay> days
) {}