package com.kitchenline.api.engine;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.kitchenline.api.engine.Category.*;

@Component
public class ProgramGenerator {

    // Order the blocks appear in every workout
    private static final Category[] BLOCK_ORDER = {LATERAL, GLUTE_HIP, SHOULDER, POWER_CORE};

    // Day A focuses on lateral, Day B on glutes, Day C on shoulders, then repeat
    private static final Category[] FOCUS_ROTATION = {LATERAL, GLUTE_HIP, SHOULDER};

    // Lookup tables indexed by tier - 1 (tier 1 = index 0)
    private static final int[] BASE_SETS = {2, 2, 3, 3, 4};
    private static final int[] BASE_SECONDS = {20, 25, 30, 35, 40};

    // Target effort (RPE) by week - 1
    private static final int[] WEEK_RPE = {6, 7, 7, 8};

    private static final String[][] WEEK_INFO = {
            {"Groove", "Learn every movement at a moderate effort. Pick loads you could lift 3-4 more reps with."},
            {"Volume", "Same exercises and loads, 2 more reps per set (or 5 more seconds). This is your first overload."},
            {"Sets", "Keep the week-2 reps and add one set to every exercise. Rest 60-90 seconds between sets."},
            {"Load", "Add 5-10% load, or slow the lowering phase to 3 seconds for bodyweight moves. Log your numbers for next month."}
    };

    private final ExerciseLibrary library;
    private final TierCalculator tierCalculator;

    // Constructor injection: Spring passes in the shared ExerciseLibrary and TierCalculator
    public ProgramGenerator(ExerciseLibrary library, TierCalculator tierCalculator) {
        this.library = library;
        this.tierCalculator = tierCalculator;
    }

    /** Builds the full 4-week program for one month of a player's subscription. */
    public MonthlyProgram generate(PlayerProfile profile, int month) {
        Intensity intensity = tierCalculator.calculate(profile, month);
        boolean courtHeavy = profile.courtSessions() >= 6;

        // 1. Choose each day's exercises ONCE. They stay the same all month,
        //    because progressive overload means doing the same moves a little harder each week.
        Map<Category, Integer> perCategory = exercisesPerCategory(profile, intensity.tier());
        List<DayTemplate> templates = new ArrayList<>();
        for (int d = 0; d < profile.workoutsPerWeek(); d++) {
            Category focus = FOCUS_ROTATION[d % FOCUS_ROTATION.length];
            Map<Category, List<Exercise>> picks = new EnumMap<>(Category.class);
            for (Category cat : BLOCK_ORDER) {
                int count = perCategory.get(cat) + (cat == focus ? 1 : 0);
                picks.put(cat, pick(cat, intensity, d, count));
            }
            String label = String.valueOf((char) ('A' + d));
            templates.add(new DayTemplate(label, focus, picks));
        }

        // 2. Apply each week's dose to those same exercises
        List<TrainingWeek> weeks = new ArrayList<>();
        for (int week = 1; week <= 4; week++) {
            List<WorkoutDay> days = new ArrayList<>();
            for (DayTemplate t : templates) {
                List<WorkoutBlock> blocks = new ArrayList<>();
                for (Category cat : BLOCK_ORDER) {
                    final int w = week;
                    List<Prescription> items = t.picks().get(cat).stream()
                            .map(e -> prescribe(e, w, intensity.tier(), profile, courtHeavy))
                            .toList();
                    blocks.add(new WorkoutBlock(cat, cat == t.focus(), items));
                }
                days.add(new WorkoutDay(t.label(), t.focus(), blocks));
            }
            weeks.add(new TrainingWeek(week, WEEK_INFO[week - 1][0], WEEK_INFO[week - 1][1], days));
        }

        return new MonthlyProgram(month, profile, intensity, weeks,
                buildNotes(profile, intensity, courtHeavy));
    }

    /** How many exercises of each category go in a normal (non-focus) block. */
    private Map<Category, Integer> exercisesPerCategory(PlayerProfile profile, int tier) {
        Map<Category, Integer> counts = new EnumMap<>(Category.class);
        counts.put(LATERAL, 2);
        counts.put(GLUTE_HIP, 2);
        counts.put(SHOULDER, 2);
        counts.put(POWER_CORE, tier >= 3 ? 2 : 1);
        // 5-6 workouts a week: keep each one shorter to protect recovery
        if (profile.workoutsPerWeek() >= 5) {
            counts.put(LATERAL, 1);
            counts.put(GLUTE_HIP, 1);
        }
        return counts;
    }

    /**
     * Picks exercises for one block. Keeps only what the player's tier unlocks
     * (and no jumps if they're not allowed), hardest first, then rotates through
     * them by day so Day A and Day B don't get identical exercises.
     * At most one jump exercise per block.
     */
    private List<Exercise> pick(Category cat, Intensity intensity, int dayIndex, int count) {
        List<Exercise> pool = library.byCategory(cat).stream()
                .filter(e -> e.minTier() <= intensity.tier())
                .filter(e -> !e.plyometric() || intensity.jumpsAllowed())
                .sorted(Comparator.comparingInt(Exercise::minTier).reversed())
                .toList();

        int window = Math.min(pool.size(), count * 3);
        List<Exercise> chosen = new ArrayList<>();
        for (int j = 0; chosen.size() < count && j < window * 2; j++) {
            Exercise e = pool.get((dayIndex * count + j) % window);
            boolean alreadyHasJump = chosen.stream().anyMatch(Exercise::plyometric);
            // Max one jump exercise per block, so strength work is never crowded out
            if (!chosen.contains(e) && !(e.plyometric() && alreadyHasJump)) {
                chosen.add(e);
            }
        }
        return chosen;
    }

    /** Sets, reps/seconds, effort and load for one exercise in one week. */
    private Prescription prescribe(Exercise e, int week, int tier, PlayerProfile profile, boolean courtHeavy) {
        // Sets: base by tier, +1 from week 3 on
        int sets = BASE_SETS[tier - 1] + (week >= 3 ? 1 : 0);
        if (courtHeavy && e.category() == GLUTE_HIP) sets--;   // lots of court time = less leg volume
        if (profile.age() >= 60) sets = Math.min(sets, 3);
        sets = Math.max(2, sets);

        // Reps or seconds: more from week 2 on
        int amount;
        if (e.unit() == Unit.SECONDS) {
            int bonus = switch (week) {
                case 2, 3 -> 5;
                case 4 -> 10;
                default -> 0;
            };
            amount = BASE_SECONDS[tier - 1] + bonus;
        } else {
            int base = e.plyometric() ? 5 : tier <= 2 ? 10 : tier <= 4 ? 8 : 6;
            amount = base + (week >= 2 ? (e.plyometric() ? 1 : 2) : 0);
        }

        int rpe = clamp(WEEK_RPE[week - 1] + (tier >= 4 ? 1 : 0) - (tier == 1 ? 1 : 0), 5, 9);

        // Load: dumbbell lower-body moves only; +10% in week 4
        Integer load = null;
        if (e.usesDumbbells() && e.category() != SHOULDER) {
            int start = startingLoad(profile.gender(), tier);
            load = week == 4 ? (int) Math.round(start * 1.1) : start;
        }

        String dose = sets + " × " + amount + (e.unit() == Unit.SECONDS ? "s" : "")
                + (e.perSide() ? " /side" : "");

        return new Prescription(e, sets, amount, dose, rpe, load);
    }

    /** Suggested starting dumbbell weight in pounds. */
    private int startingLoad(Gender gender, int tier) {
        int[] table = switch (gender) {
            case MALE -> new int[]{10, 15, 20, 25, 35};
            case FEMALE -> new int[]{5, 8, 12, 15, 25};
            case UNSPECIFIED -> new int[]{8, 12, 15, 20, 30};
        };
        return table[tier - 1];
    }

    /** Coaching notes shown at the top of the program. */
    private List<String> buildNotes(PlayerProfile profile, Intensity intensity, boolean courtHeavy) {
        List<String> notes = new ArrayList<>();
        notes.add("Every session covers the three foundations: glutes and hips, shoulder stability, and lateral movement.");

        if (intensity.jumpsAllowed()) {
            notes.add("Jump training is included. Do it first, while you're fresh, and stop a set when landings get loud.");
        } else if (profile.age() >= 55) {
            notes.add("Jumps are replaced with controlled strength and reaction work to protect knees and Achilles tendons.");
        }

        if (courtHeavy) {
            notes.add("You're on court " + profile.courtSessions() + " times a week, so lower-body strength volume "
                    + "is trimmed by one set. Avoid heavy leg work the day before a match.");
        }

        int total = profile.courtSessions() + profile.workoutsPerWeek();
        if (total >= 11) {
            notes.add(total + " sessions a week is a lot. Keep at least one full rest day and sleep 7+ hours.");
        }

        if (profile.courtSessions() == 0) {
            notes.add("No court time logged. Add at least one play session a week so the movement work carries over to games.");
        }

        notes.add("Starting dumbbell guide: about " + startingLoad(profile.gender(), intensity.tier())
                + " lb for lower-body moves, much lighter (2-8 lb) for shoulder work.");
        return notes;
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Internal helper: one day's chosen exercises before weekly doses are applied. */
    private record DayTemplate(String label, Category focus, Map<Category, List<Exercise>> picks) {}
}