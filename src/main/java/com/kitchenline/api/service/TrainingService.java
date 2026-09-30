package com.kitchenline.api.service;

import com.kitchenline.api.domain.*;
import com.kitchenline.api.engine.MonthlyProgram;
import com.kitchenline.api.engine.PlayerProfile;
import com.kitchenline.api.engine.ProgramGenerator;
import com.kitchenline.api.repository.AppUserRepository;
import com.kitchenline.api.repository.ProfileRepository;
import com.kitchenline.api.repository.ProgramMonthRepository;
import com.kitchenline.api.repository.WorkoutLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * The app's actions: save a profile, show the program, log workouts, start the next month.
 * Business rules live here, so every screen (web now, phone app later) follows the same rules.
 */
@Service
@Transactional
public class TrainingService {

    /** Share of a month's workouts needed before the next month unlocks. */
    static final double UNLOCK_THRESHOLD = 0.75;

    private final AppUserRepository users;
    private final ProfileRepository profiles;
    private final ProgramMonthRepository months;
    private final WorkoutLogRepository logs;
    private final ProgramGenerator generator;

    public TrainingService(AppUserRepository users, ProfileRepository profiles,
                           ProgramMonthRepository months, WorkoutLogRepository logs,
                           ProgramGenerator generator) {
        this.users = users;
        this.profiles = profiles;
        this.months = months;
        this.logs = logs;
        this.generator = generator;
    }

    /**
     * Creates or updates the player's answers.
     * First time: also starts Month 1.
     * Later: the current month only changes if no workouts have been logged yet.
     */
    public Profile saveProfile(Long userId, PlayerProfile answers) {
        AppUser user = findUser(userId);

        Profile profile = profiles.findByUserId(userId).orElse(null);
        if (profile == null) {
            profile = profiles.save(new Profile(user, answers));
        } else {
            profile.update(answers);
        }

        ProgramMonth current = months.findTopByUserIdOrderByMonthNumberDesc(userId).orElse(null);
        if (current == null) {
            months.save(new ProgramMonth(user, 1, answers));
        } else if (logs.countByProgramMonthId(current.getId()) == 0) {
            current.replaceSnapshot(answers);
        }
        return profile;
    }

    /** The current month's program plus progress, ready for the app screen. */
    @Transactional(readOnly = true)
    public ProgramView getCurrentProgram(Long userId) {
        AppUser user = findUser(userId);
        ProgramMonth month = currentMonth(userId);

        MonthlyProgram program = generator.generate(month.getProfileSnapshot(), month.getMonthNumber());

        List<String> completed = logs.findByProgramMonthId(month.getId()).stream()
                .sorted(Comparator.comparingInt(WorkoutLog::getWeekNumber)
                        .thenComparing(WorkoutLog::getDayLabel))
                .map(log -> log.getWeekNumber() + log.getDayLabel())
                .toList();

        int total = totalWorkouts(month);
        int done = completed.size();
        return new ProgramView(program, unlockedWeeks(user), completed, done, total,
                done * 100 / total, canStartNextMonth(user, done, total));
    }

    /** Marks one workout done. Doing it twice is harmless. */
    public void markDone(Long userId, int week, String dayLabel) {
        AppUser user = findUser(userId);
        ProgramMonth month = currentMonth(userId);
        String day = validateDay(month, dayLabel);

        if (week < 1 || week > 4) {
            throw new IllegalArgumentException("Week must be 1-4");
        }
        if (week > unlockedWeeks(user)) {
            throw new IllegalStateException("Week " + week + " is for members. Start the monthly plan to unlock it.");
        }
        if (!logs.existsByProgramMonthIdAndWeekNumberAndDayLabel(month.getId(), week, day)) {
            logs.save(new WorkoutLog(month, week, day));
        }
    }

    /** Un-marks a workout that was checked by mistake. */
    public void unmarkDone(Long userId, int week, String dayLabel) {
        ProgramMonth month = currentMonth(userId);
        logs.deleteByProgramMonthIdAndWeekNumberAndDayLabel(month.getId(), week, validateDay(month, dayLabel));
    }

    /** Members who finished 75% of this month move on, using their latest profile answers. */
    public ProgramMonth startNextMonth(Long userId) {
        AppUser user = findUser(userId);
        ProgramMonth current = currentMonth(userId);

        if (user.getSubscriptionStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("A membership is required to start a new month.");
        }
        int done = (int) logs.countByProgramMonthId(current.getId());
        if (!canStartNextMonth(user, done, totalWorkouts(current))) {
            throw new IllegalStateException("Finish at least 75% of this month's workouts first.");
        }

        PlayerProfile latest = profiles.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Fill in your player profile first."))
                .toPlayerProfile();
        return months.save(new ProgramMonth(user, current.getMonthNumber() + 1, latest));
    }

    // ---------- Rules and helpers ----------

    int unlockedWeeks(AppUser user) {
        return user.getSubscriptionStatus() == SubscriptionStatus.ACTIVE ? 4 : 1;
    }

    private boolean canStartNextMonth(AppUser user, int done, int total) {
        return user.getSubscriptionStatus() == SubscriptionStatus.ACTIVE
                && done >= Math.ceil(total * UNLOCK_THRESHOLD);
    }

    private int totalWorkouts(ProgramMonth month) {
        return month.getProfileSnapshot().workoutsPerWeek() * 4;
    }

    /** Turns "b" into "B" and checks the day exists (3 workouts a week = A, B, C). */
    private String validateDay(ProgramMonth month, String dayLabel) {
        if (dayLabel == null || dayLabel.length() != 1) {
            throw new IllegalArgumentException("Day must be a single letter, like A");
        }
        String day = dayLabel.toUpperCase();
        int index = day.charAt(0) - 'A';
        if (index < 0 || index >= month.getProfileSnapshot().workoutsPerWeek()) {
            throw new IllegalArgumentException("Day " + day + " isn't part of this program");
        }
        return day;
    }

    private AppUser findUser(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user with id " + userId));
    }

    private ProgramMonth currentMonth(Long userId) {
        return months.findTopByUserIdOrderByMonthNumberDesc(userId)
                .orElseThrow(() -> new IllegalStateException("Fill in your player profile first."));
    }
}