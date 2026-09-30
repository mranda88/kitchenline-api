package com.kitchenline.api.controller;

import com.kitchenline.api.engine.PlayerProfile;
import com.kitchenline.api.security.AccountPrincipal;
import com.kitchenline.api.service.TrainingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final TrainingService training;

    public ProfileController(TrainingService training) {
        this.training = training;
    }

    /** Loads the player's answers into the profile form. 404 if they haven't filled it in yet. */
    @GetMapping
    public ResponseEntity<?> get(@AuthenticationPrincipal AccountPrincipal me) {
        Optional<PlayerProfile> profile = training.getProfile(me.getId());
        if (profile.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No profile yet. Fill in your player profile to get your program."));
        }
        return ResponseEntity.ok(profile.get());
    }

    /** Saves the six answers. The first save also starts Month 1. */
    @PutMapping
    public PlayerProfile save(@AuthenticationPrincipal AccountPrincipal me,
                              @RequestBody ProfileRequest request) {
        PlayerProfile answers = request.toPlayerProfile();
        training.saveProfile(me.getId(), answers);
        return answers;
    }
}