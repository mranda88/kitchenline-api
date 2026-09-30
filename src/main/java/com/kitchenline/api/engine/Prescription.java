package com.kitchenline.api.engine;

/** One exercise with this week's dose, e.g. "Bulgarian split squat — 3 × 10 /side at RPE 7". */
public record Prescription(
        Exercise exercise,
        int sets,
        int amount,               // reps, or seconds if exercise.unit() is SECONDS
        String dose,              // ready to display, e.g. "3 × 10 /side" or "2 × 30s"
        int rpe,                  // target effort, 1-10 (Rate of Perceived Exertion)
        Integer suggestedLoadLbs  // starting dumbbell weight; null when not applicable
) {}