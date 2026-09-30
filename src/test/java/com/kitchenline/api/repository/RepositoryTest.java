package com.kitchenline.api.repository;

import com.kitchenline.api.domain.*;
import com.kitchenline.api.engine.Gender;
import com.kitchenline.api.engine.PlayerProfile;
import com.kitchenline.api.engine.SkillLevel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional   // each test's rows are rolled back afterwards, so your tables stay clean
class RepositoryTest {

    @Autowired AppUserRepository users;
    @Autowired ProfileRepository profiles;
    @Autowired ProgramMonthRepository months;
    @Autowired WorkoutLogRepository logs;

    private final PlayerProfile answers =
            new PlayerProfile(34, Gender.FEMALE, SkillLevel.INTERMEDIATE, 2, 3, 3);

    @Test
    void savesUserAndFindsByEmailIgnoringCase() {
        users.save(new AppUser("Test@Example.com", "not-a-real-hash"));

        AppUser found = users.findByEmail("test@example.com").orElseThrow();
        assertEquals(SubscriptionStatus.TRIAL, found.getSubscriptionStatus());
        assertNotNull(found.getCreatedAt());
        assertTrue(users.existsByEmail("test@example.com"));
    }

    @Test
    void profileSurvivesARoundTripToTheDatabase() {
        AppUser user = users.save(new AppUser("profile@example.com", "x"));
        profiles.save(new Profile(user, answers));

        Profile loaded = profiles.findByUserId(user.getId()).orElseThrow();
        assertEquals(answers, loaded.toPlayerProfile());
    }

    @Test
    void currentMonthIsTheHighestMonthNumber() {
        AppUser user = users.save(new AppUser("months@example.com", "x"));
        months.save(new ProgramMonth(user, 1, answers));
        months.save(new ProgramMonth(user, 2, answers));

        ProgramMonth current = months.findTopByUserIdOrderByMonthNumberDesc(user.getId()).orElseThrow();
        assertEquals(2, current.getMonthNumber());
    }

    @Test
    void duplicateMonthIsRejectedByTheDatabase() {
        AppUser user = users.save(new AppUser("dupe@example.com", "x"));
        months.saveAndFlush(new ProgramMonth(user, 1, answers));

        assertThrows(DataIntegrityViolationException.class,
                () -> months.saveAndFlush(new ProgramMonth(user, 1, answers)));
    }

    @Test
    void workoutLogsAreCountedPerMonth() {
        AppUser user = users.save(new AppUser("logs@example.com", "x"));
        ProgramMonth month = months.save(new ProgramMonth(user, 1, answers));
        logs.save(new WorkoutLog(month, 1, "A"));
        logs.save(new WorkoutLog(month, 1, "B"));

        assertEquals(2, logs.countByProgramMonthId(month.getId()));
        assertTrue(logs.existsByProgramMonthIdAndWeekNumberAndDayLabel(month.getId(), 1, "A"));
        assertFalse(logs.existsByProgramMonthIdAndWeekNumberAndDayLabel(month.getId(), 2, "A"));
    }
}