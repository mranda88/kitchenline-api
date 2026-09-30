package com.kitchenline.api.engine;

public record Exercise(
        String id,              // short unique key, e.g. "bss"
        String name,            // shown to the user, e.g. "Bulgarian split squat"
        Category category,      // which training area it belongs to
        int minTier,            // lowest intensity tier (1-5) allowed to get it
        Unit unit,              // reps or seconds
        boolean perSide,        // true = do it on each side ("/side")
        boolean plyometric,     // true = jumping; withheld from older or lower-tier players
        boolean usesDumbbells,  // true = show a starting-weight suggestion
        String cue              // one-line coaching tip
) {}
