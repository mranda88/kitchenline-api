package com.kitchenline.api.engine;

import org.springframework.stereotype.Component;

import java.util.List;

import static com.kitchenline.api.engine.Category.*;
import static com.kitchenline.api.engine.Unit.*;

@Component
public class ExerciseLibrary {

    // Readable names for the true/false flags below
    private static final boolean SIDE = true, BOTH = false;
    private static final boolean JUMP = true, NO_JUMP = false;
    private static final boolean DB = true, NO_DB = false;

    private final List<Exercise> exercises = List.of(

            // ---------- Lateral movement ----------
            new Exercise("bandwalk", "Lateral band walk", LATERAL, 1, REPS, SIDE, NO_JUMP, NO_DB,
                    "Band above knees, quarter squat, toes forward. Step wide without letting your feet snap together."),
            new Exercise("splitshuffle", "Split-step to shuffle", LATERAL, 1, SECONDS, BOTH, NO_JUMP, NO_DB,
                    "Small hop, land on the balls of your feet as a partner calls a direction. Two shuffles, return, repeat."),
            new Exercise("latlunge", "Lateral lunge", LATERAL, 2, REPS, SIDE, NO_JUMP, DB,
                    "Sit back into one hip, other leg straight. Push the floor away to return."),
            new Exercise("crossover", "Crossover step & recover", LATERAL, 2, REPS, SIDE, NO_JUMP, NO_DB,
                    "From ready position, crossover step to chase a wide ball, plant, recover to center in two steps."),
            new Exercise("reactshuffle", "Reaction shuffle ladder", LATERAL, 3, SECONDS, BOTH, NO_JUMP, NO_DB,
                    "Shuffle between two cones 3 m apart. Partner points; change direction on the point, not before."),
            new Exercise("dropstep", "Kitchen-line drop step", LATERAL, 3, REPS, SIDE, NO_JUMP, NO_DB,
                    "Start at the line, open the hip and drop-step back for a lob, then close back to the line."),
            new Exercise("skater", "Skater bound", LATERAL, 4, REPS, SIDE, JUMP, NO_DB,
                    "Bound sideways, stick the landing for one second on the outside leg. Knee tracks over toes."),
            new Exercise("hurdlehop", "Lateral mini-hurdle hops", LATERAL, 4, REPS, SIDE, JUMP, NO_DB,
                    "Quick, low hops over a 15 cm hurdle. Minimal ground time, stay tall."),
            new Exercise("latbound", "Lateral bound to split-step", LATERAL, 5, REPS, SIDE, JUMP, NO_DB,
                    "Max-distance bound, stick, then an immediate split-step. Power, then control."),

            // ---------- Glute & hip strength ----------
            new Exercise("bridge", "Glute bridge", GLUTE_HIP, 1, REPS, BOTH, NO_JUMP, NO_DB,
                    "Ribs down, drive through heels, squeeze for 2 seconds at the top."),
            new Exercise("clam", "Banded clamshell", GLUTE_HIP, 1, REPS, SIDE, NO_JUMP, NO_DB,
                    "Hips stacked, don't roll back. Slow on the way down."),
            new Exercise("slbridge", "Single-leg glute bridge", GLUTE_HIP, 2, REPS, SIDE, NO_JUMP, NO_DB,
                    "Hips level the whole time. If one side drops, shorten the range."),
            new Exercise("goblet", "Goblet squat", GLUTE_HIP, 2, REPS, BOTH, NO_JUMP, DB,
                    "Dumbbell at chest, sit between your hips, knees out over toes."),
            new Exercise("airplane", "Hip airplane", GLUTE_HIP, 3, REPS, SIDE, NO_JUMP, NO_DB,
                    "Hinge on one leg, rotate the pelvis open and closed. Hold a wall at first."),
            new Exercise("bss", "Bulgarian split squat", GLUTE_HIP, 3, REPS, SIDE, NO_JUMP, DB,
                    "Rear foot on a bench, front shin slightly forward, lower under control."),
            new Exercise("slrdl", "Single-leg RDL", GLUTE_HIP, 3, REPS, SIDE, NO_JUMP, DB,
                    "Hips square to the floor, reach the weight to mid-shin, feel the hamstring and glute."),
            new Exercise("thrust", "Hip thrust", GLUTE_HIP, 3, REPS, BOTH, NO_JUMP, DB,
                    "Upper back on a bench, weight on hips. Chin tucked, full lockout, no low-back arch."),
            new Exercise("copen", "Copenhagen plank", GLUTE_HIP, 4, SECONDS, SIDE, NO_JUMP, NO_DB,
                    "Top leg on a bench, lift hips. Start with the knee on the bench. Builds the groin for wide lunges."),
            new Exercise("deadlift", "Dumbbell or trap-bar deadlift", GLUTE_HIP, 4, REPS, BOTH, NO_JUMP, DB,
                    "Brace, push the floor away. Keep the weight close to your legs."),
            new Exercise("ssjump", "Split-squat jump", GLUTE_HIP, 5, REPS, SIDE, JUMP, NO_DB,
                    "Jump and switch legs in the air. Soft, quiet landings; stop the set when landings get loud."),

            // ---------- Shoulder stability ----------
            new Exercise("bander", "Band external rotation", SHOULDER, 1, REPS, SIDE, NO_JUMP, NO_DB,
                    "Elbow pinned to your side with a towel roll. Rotate out slowly, 3 seconds back."),
            new Exercise("wallslide", "Scapular wall slide", SHOULDER, 1, REPS, BOTH, NO_JUMP, NO_DB,
                    "Forearms on the wall, slide up while shoulder blades wrap around the ribs."),
            new Exercise("pullapart", "Band pull-apart", SHOULDER, 1, REPS, BOTH, NO_JUMP, NO_DB,
                    "Arms straight, pull the band to your chest. Don't shrug."),
            new Exercise("ytw", "Prone Y-T-W", SHOULDER, 2, REPS, BOTH, NO_JUMP, NO_DB,
                    "Face down, thumbs up. Small range, perfect form. Light plates or none."),
            new Exercise("sidelyer", "Side-lying dumbbell ER", SHOULDER, 2, REPS, SIDE, NO_JUMP, DB,
                    "Very light dumbbell. Rotate up, lower in 3 seconds. Protects the rotator cuff for overheads."),
            new Exercise("taps", "Plank shoulder taps", SHOULDER, 2, REPS, SIDE, NO_JUMP, NO_DB,
                    "Feet wide, hips don't sway as you tap the opposite shoulder."),
            new Exercise("er9090", "90/90 band ER hold with pulses", SHOULDER, 3, SECONDS, SIDE, NO_JUMP, NO_DB,
                    "Arm at 90 degrees out and up, band pulling forward. Hold, then small pulses. Mirrors the overhead smash."),
            new Exercise("landmine", "Half-kneeling single-arm press", SHOULDER, 3, REPS, SIDE, NO_JUMP, DB,
                    "Press up and slightly forward, ribs down, glute of the down knee squeezed."),
            new Exercise("kbcarry", "Bottoms-up kettlebell carry", SHOULDER, 4, SECONDS, SIDE, NO_JUMP, NO_DB,
                    "Kettlebell upside down at shoulder height. Grip and rotator cuff work together. Walk slowly."),
            new Exercise("mbcatch", "Wall ball rebound catch (overhead)", SHOULDER, 4, REPS, SIDE, NO_JUMP, NO_DB,
                    "Small ball, 90/90 arm position, rapid throws and catches. Slow the ball down on every catch."),

            // ---------- Paddle power & core ----------
            new Exercise("deadbug", "Dead bug", POWER_CORE, 1, REPS, SIDE, NO_JUMP, NO_DB,
                    "Low back pressed into the floor. Exhale fully as you extend."),
            new Exercise("pallof", "Pallof press", POWER_CORE, 1, REPS, SIDE, NO_JUMP, NO_DB,
                    "Band anchored to the side, press straight out and resist the twist."),
            new Exercise("sideplank", "Side plank", POWER_CORE, 2, SECONDS, SIDE, NO_JUMP, NO_DB,
                    "Straight line from head to heels. Top hip forward, not sagging back."),
            new Exercise("chop", "Half-kneeling band chop", POWER_CORE, 2, REPS, SIDE, NO_JUMP, NO_DB,
                    "High to low across the body. Rotation comes from the torso, not the arms."),
            new Exercise("mbrot", "Med-ball rotational throw", POWER_CORE, 3, REPS, SIDE, NO_JUMP, NO_DB,
                    "Load the back hip, throw into a wall like a two-handed backhand drive."),
            new Exercise("mbscoop", "Med-ball scoop toss", POWER_CORE, 4, REPS, SIDE, NO_JUMP, NO_DB,
                    "Low athletic stance, scoop from the hip. Legs start it, hips finish it."),
            new Exercise("mbsplit", "Split-step to med-ball throw", POWER_CORE, 5, REPS, SIDE, JUMP, NO_DB,
                    "Split-step, read a call, turn and throw. Reaction plus power, like a fast hands exchange.")
    );

    /** Every exercise in the library. */
    public List<Exercise> all() {
        return exercises;
    }

    /** Only the exercises in one training area, e.g. all SHOULDER moves. */
    public List<Exercise> byCategory(Category category) {
        return exercises.stream()
                .filter(e -> e.category() == category)
                .toList();
    }
}