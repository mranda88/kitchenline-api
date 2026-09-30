package com.kitchenline.api.controller;

import com.kitchenline.api.security.AccountPrincipal;
import com.kitchenline.api.service.ProgramView;
import com.kitchenline.api.service.TrainingService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/program")
public class ProgramController {

    private final TrainingService training;

    public ProgramController(TrainingService training) {
        this.training = training;
    }

    /** This month's program, progress and unlocked weeks. */
    @GetMapping("/current")
    public ProgramView current(@AuthenticationPrincipal AccountPrincipal me) {
        return training.getCurrentProgram(me.getId());
    }

    /** Check off a workout, e.g. POST /api/program/workouts/2/B */
    @PostMapping("/workouts/{week}/{day}")
    public ProgramView complete(@AuthenticationPrincipal AccountPrincipal me,
                                @PathVariable("week") int week,
                                @PathVariable("day") String day) {
        training.markDone(me.getId(), week, day);
        return training.getCurrentProgram(me.getId());
    }

    /** Un-check a workout. */
    @DeleteMapping("/workouts/{week}/{day}")
    public ProgramView uncomplete(@AuthenticationPrincipal AccountPrincipal me,
                                  @PathVariable("week") int week,
                                  @PathVariable("day") String day) {
        training.unmarkDone(me.getId(), week, day);
        return training.getCurrentProgram(me.getId());
    }

    /** Start the next month (members who finished 75%). */
    @PostMapping("/next-month")
    public ProgramView nextMonth(@AuthenticationPrincipal AccountPrincipal me) {
        training.startNextMonth(me.getId());
        return training.getCurrentProgram(me.getId());
    }
}