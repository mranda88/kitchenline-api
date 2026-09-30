package com.kitchenline.api.service;

import com.kitchenline.api.engine.MonthlyProgram;

import java.util.List;

/** Everything the app screen needs: the program plus the player's progress and access. */
public record ProgramView(
        MonthlyProgram program,
        int unlockedWeeks,        // 1 on the free trial, 4 for members
        List<String> completed,   // finished workouts, e.g. ["1A", "1B", "2A"]
        int completedCount,
        int totalWorkouts,
        int percentComplete,
        boolean canStartNextMonth
) {}