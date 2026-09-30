package com.kitchenline.api.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProgramGeneratorTest {

    private ProgramGenerator generator;

    // A typical customer: 34-year-old 3.5 player, 2 drills + 3 games + 3 workouts a week
    private final PlayerProfile typical =
            new PlayerProfile(34, Gender.FEMALE, SkillLevel.INTERMEDIATE, 2, 3, 3);

    @BeforeEach
    void setUp() {
        // No Spring needed: build the pieces by hand, exactly as Spring would
        generator = new ProgramGenerator(new ExerciseLibrary(), new TierCalculator());
    }

    // ---------- Structure ----------

    @Test
    void programHasFourWeeksWithOneDayPerRequestedWorkout() {
        MonthlyProgram program = generator.generate(typical, 1);
        assertEquals(4, program.weeks().size());
        for (TrainingWeek week : program.weeks()) {
            assertEquals(3, week.days().size());
        }
    }

    @Test
    void everyWorkoutCoversAllThreeFoundations() {
        MonthlyProgram program = generator.generate(typical, 1);
        for (TrainingWeek week : program.weeks()) {
            for (WorkoutDay day : week.days()) {
                for (Category foundation : List.of(Category.LATERAL, Category.GLUTE_HIP, Category.SHOULDER)) {
                    assertTrue(hasExercisesFor(day, foundation),
                            "Day " + day.label() + " is missing " + foundation);
                }
            }
        }
    }

    @Test
    void exercisesStayTheSameAllMonth() {
        MonthlyProgram program = generator.generate(typical, 1);
        List<String> week1 = exerciseIds(program.weeks().get(0).days().get(0));
        List<String> week4 = exerciseIds(program.weeks().get(3).days().get(0));
        assertEquals(week1, week4);
    }

    // ---------- Progressive overload ----------

    @Test
    void week2AddsTwoReps() {
        Prescription w1 = firstRepsExercise(generator.generate(typical, 1), 1);
        Prescription w2 = firstRepsExercise(generator.generate(typical, 1), 2);
        assertEquals(w1.amount() + 2, w2.amount());
        assertEquals(w1.sets(), w2.sets());
    }

    @Test
    void week3AddsOneSet() {
        MonthlyProgram program = generator.generate(typical, 1);
        Prescription w2 = first(program, 2);
        Prescription w3 = first(program, 3);
        assertEquals(w2.sets() + 1, w3.sets());
    }

    @Test
    void week4IncreasesDumbbellLoad() {
        MonthlyProgram program = generator.generate(typical, 1);
        Prescription w1 = firstWithLoad(program, 1);
        Prescription w4 = firstWithLoad(program, 4);
        assertTrue(w4.suggestedLoadLbs() > w1.suggestedLoadLbs(),
                "Week 4 load should be higher than week 1");
    }

    // ---------- Safety rules ----------

    @Test
    void noJumpsForPlayers55AndOver() {
        PlayerProfile older = new PlayerProfile(58, Gender.MALE, SkillLevel.EXPERT, 3, 3, 4);
        allPrescriptions(generator.generate(older, 1))
                .forEach(p -> assertFalse(p.exercise().plyometric(),
                        p.exercise().name() + " is a jump exercise"));
    }

    @Test
    void neverAssignsExercisesAboveThePlayersTier() {
        PlayerProfile beginner = new PlayerProfile(40, Gender.FEMALE, SkillLevel.BEGINNER, 1, 2, 3);
        MonthlyProgram program = generator.generate(beginner, 1);
        int tier = program.intensity().tier();
        allPrescriptions(program)
                .forEach(p -> assertTrue(p.exercise().minTier() <= tier,
                        p.exercise().name() + " is above tier " + tier));
    }

    @Test
    void everyProfileCombinationProducesAValidProgram() {
        // Every skill x age group x workouts per week x first 6 months
        for (SkillLevel skill : SkillLevel.values()) {
            for (int age : new int[]{18, 30, 45, 58, 65, 80}) {
                for (int workouts = 1; workouts <= 6; workouts++) {
                    for (int month = 1; month <= 6; month++) {
                        PlayerProfile p = new PlayerProfile(age, Gender.UNSPECIFIED, skill, 3, 3, workouts);
                        MonthlyProgram program = generator.generate(p, month);
                        for (TrainingWeek week : program.weeks()) {
                            for (WorkoutDay day : week.days()) {
                                for (WorkoutBlock block : day.blocks()) {
                                    assertFalse(block.exercises().isEmpty(), "Empty block");
                                    long jumps = block.exercises().stream()
                                            .filter(x -> x.exercise().plyometric()).count();
                                    assertTrue(jumps <= 1, "More than one jump in a block");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    void invalidProfileIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new PlayerProfile(10, Gender.MALE, SkillLevel.BEGINNER, 1, 1, 3));
        assertThrows(IllegalArgumentException.class,
                () -> new PlayerProfile(30, Gender.MALE, SkillLevel.BEGINNER, 1, 1, 9));
    }

    // ---------- Helpers ----------

    private boolean hasExercisesFor(WorkoutDay day, Category category) {
        return day.blocks().stream()
                .anyMatch(b -> b.category() == category && !b.exercises().isEmpty());
    }

    private List<String> exerciseIds(WorkoutDay day) {
        return day.blocks().stream()
                .flatMap(b -> b.exercises().stream())
                .map(p -> p.exercise().id())
                .toList();
    }

    private List<Prescription> dayOne(MonthlyProgram program, int week) {
        return program.weeks().get(week - 1).days().get(0).blocks().stream()
                .flatMap(b -> b.exercises().stream())
                .toList();
    }

    private Prescription first(MonthlyProgram program, int week) {
        return dayOne(program, week).get(0);
    }

    private Prescription firstRepsExercise(MonthlyProgram program, int week) {
        return dayOne(program, week).stream()
                .filter(p -> p.exercise().unit() == Unit.REPS && !p.exercise().plyometric())
                .findFirst().orElseThrow();
    }

    private Prescription firstWithLoad(MonthlyProgram program, int week) {
        return dayOne(program, week).stream()
                .filter(p -> p.suggestedLoadLbs() != null)
                .findFirst().orElseThrow();
    }

    private List<Prescription> allPrescriptions(MonthlyProgram program) {
        return program.weeks().stream()
                .flatMap(w -> w.days().stream())
                .flatMap(d -> d.blocks().stream())
                .flatMap(b -> b.exercises().stream())
                .toList();
    }
}