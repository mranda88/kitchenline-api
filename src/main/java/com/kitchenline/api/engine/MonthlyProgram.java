package com.kitchenline.api.engine;

import java.util.List;

/** Everything the user gets for one month of their subscription. */
public record MonthlyProgram(
        int month,
        PlayerProfile profile,
        Intensity intensity,
        List<TrainingWeek> weeks,
        List<String> notes
) {}