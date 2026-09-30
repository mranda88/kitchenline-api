package com.kitchenline.api.engine;

import java.util.List;

/** One section of a workout, e.g. all the shoulder exercises for that day. */
public record WorkoutBlock(
        Category category,
        boolean focus,            // true = this day's emphasis (gets one extra exercise)
        List<Prescription> exercises
) {}