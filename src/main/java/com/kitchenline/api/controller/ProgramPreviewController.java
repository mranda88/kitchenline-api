package com.kitchenline.api.controller;

import com.kitchenline.api.engine.Gender;
import com.kitchenline.api.engine.MonthlyProgram;
import com.kitchenline.api.engine.PlayerProfile;
import com.kitchenline.api.engine.ProgramGenerator;
import com.kitchenline.api.engine.SkillLevel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * TEMPORARY: lets us see a generated program in the browser.
 * In step 5 this is replaced by real endpoints tied to a logged-in user.
 */
@RestController
@RequestMapping("/api/program")
public class ProgramPreviewController {

    private final ProgramGenerator generator;

    public ProgramPreviewController(ProgramGenerator generator) {
        this.generator = generator;
    }

    @GetMapping("/preview")
    public MonthlyProgram preview(
            @RequestParam("age") int age,
            @RequestParam("gender") Gender gender,
            @RequestParam("skill") SkillLevel skill,
            @RequestParam(value = "drills", defaultValue = "2") int drills,
            @RequestParam(value = "plays", defaultValue = "3") int plays,
            @RequestParam(value = "workouts", defaultValue = "3") int workouts,
            @RequestParam(value = "month", defaultValue = "1") int month) {

        PlayerProfile profile = new PlayerProfile(age, gender, skill, drills, plays, workouts);
        return generator.generate(profile, month);
    }

    /** Bad input (e.g. age=10) returns a clear 400 error instead of a crash page. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badInput(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}