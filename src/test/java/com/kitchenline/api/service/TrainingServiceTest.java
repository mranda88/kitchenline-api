package com.kitchenline.api.service;

import com.kitchenline.api.domain.AppUser;
import com.kitchenline.api.domain.ProgramMonth;
import com.kitchenline.api.domain.SubscriptionStatus;
import com.kitchenline.api.engine.Gender;
import com.kitchenline.api.engine.PlayerProfile;
import com.kitchenline.api.engine.SkillLevel;
import com.kitchenline.api.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "app.paywall-enabled=true")
@Transactional   // everything is rolled back after each test
class TrainingServiceTest {

    @Autowired TrainingService service;
    @Autowired AppUserRepository users;

    // 3 workouts a week = 12 per month; 75% = 9
    private final PlayerProfile answers =
            new PlayerProfile(34, Gender.FEMALE, SkillLevel.INTERMEDIATE, 2, 3, 3);

    private Long newUser(SubscriptionStatus status) {
        AppUser user = new AppUser("player" + System.nanoTime() + "@example.com", "x");
        user.setSubscriptionStatus(status);
        return users.save(user).getId();
    }

    @Test
    void savingAProfileStartsMonth1() {
        Long id = newUser(SubscriptionStatus.TRIAL);
        service.saveProfile(id, answers);

        ProgramView view = service.getCurrentProgram(id);
        assertEquals(1, view.program().month());
        assertEquals(12, view.totalWorkouts());
        assertEquals(0, view.completedCount());
    }

    @Test
    void trialUsersCanOnlyLogWeek1() {
        Long id = newUser(SubscriptionStatus.TRIAL);
        service.saveProfile(id, answers);

        service.markDone(id, 1, "A");
        assertThrows(IllegalStateException.class, () -> service.markDone(id, 2, "A"));
        assertEquals(1, service.getCurrentProgram(id).unlockedWeeks());
    }

    @Test
    void trialUsersOnlyReceiveWeek1Content() {
        Long id = newUser(SubscriptionStatus.TRIAL);
        service.saveProfile(id, answers);

        assertEquals(1, service.getCurrentProgram(id).program().weeks().size());
    }

    @Test
    void membersReceiveAllFourWeeks() {
        Long id = newUser(SubscriptionStatus.ACTIVE);
        service.saveProfile(id, answers);

        assertEquals(4, service.getCurrentProgram(id).program().weeks().size());
    }

    @Test
    void markingTheSameWorkoutTwiceCountsOnce() {
        Long id = newUser(SubscriptionStatus.ACTIVE);
        service.saveProfile(id, answers);

        service.markDone(id, 1, "A");
        service.markDone(id, 1, "a");   // lowercase is accepted too

        assertEquals(1, service.getCurrentProgram(id).completedCount());
    }

    @Test
    void unmarkingRemovesTheWorkout() {
        Long id = newUser(SubscriptionStatus.ACTIVE);
        service.saveProfile(id, answers);

        service.markDone(id, 2, "B");
        service.unmarkDone(id, 2, "B");

        assertEquals(0, service.getCurrentProgram(id).completedCount());
    }

    @Test
    void daysOutsideTheProgramAreRejected() {
        Long id = newUser(SubscriptionStatus.ACTIVE);
        service.saveProfile(id, answers);   // 3 workouts = days A, B, C

        assertThrows(IllegalArgumentException.class, () -> service.markDone(id, 1, "D"));
    }

    @Test
    void editingTheProfileBeforeAnyWorkoutUpdatesTheCurrentMonth() {
        Long id = newUser(SubscriptionStatus.ACTIVE);
        service.saveProfile(id, answers);

        PlayerProfile fixed = new PlayerProfile(34, Gender.FEMALE, SkillLevel.INTERMEDIATE, 2, 3, 4);
        service.saveProfile(id, fixed);

        assertEquals(16, service.getCurrentProgram(id).totalWorkouts());
    }

    @Test
    void editingTheProfileMidMonthKeepsTheCurrentProgram() {
        Long id = newUser(SubscriptionStatus.ACTIVE);
        service.saveProfile(id, answers);
        service.markDone(id, 1, "A");

        PlayerProfile changed = new PlayerProfile(34, Gender.FEMALE, SkillLevel.INTERMEDIATE, 2, 3, 5);
        service.saveProfile(id, changed);

        assertEquals(12, service.getCurrentProgram(id).totalWorkouts());
    }

    @Test
    void nextMonthStaysLockedBelow75Percent() {
        Long id = newUser(SubscriptionStatus.ACTIVE);
        service.saveProfile(id, answers);
        completeWorkouts(id, 8);   // 8 of 12 = 67%

        assertFalse(service.getCurrentProgram(id).canStartNextMonth());
        assertThrows(IllegalStateException.class, () -> service.startNextMonth(id));
    }

    @Test
    void nextMonthUnlocksAt75PercentAndUsesTheLatestProfile() {
        Long id = newUser(SubscriptionStatus.ACTIVE);
        service.saveProfile(id, answers);
        completeWorkouts(id, 9);   // 9 of 12 = 75%

        PlayerProfile improved = new PlayerProfile(34, Gender.FEMALE, SkillLevel.ADVANCED, 2, 3, 3);
        service.saveProfile(id, improved);

        assertTrue(service.getCurrentProgram(id).canStartNextMonth());
        ProgramMonth month2 = service.startNextMonth(id);
        assertEquals(2, month2.getMonthNumber());
        assertEquals(SkillLevel.ADVANCED, month2.getProfileSnapshot().skill());
        assertEquals(0, service.getCurrentProgram(id).completedCount());
    }

    @Test
    void trialUsersCannotStartANewMonth() {
        Long id = newUser(SubscriptionStatus.TRIAL);
        service.saveProfile(id, answers);

        assertThrows(IllegalStateException.class, () -> service.startNextMonth(id));
    }

    /** Checks off the first n workouts in order: 1A, 1B, 1C, 2A, ... */
    private void completeWorkouts(Long userId, int n) {
        String[] days = {"A", "B", "C"};
        for (int i = 0; i < n; i++) {
            service.markDone(userId, i / 3 + 1, days[i % 3]);
        }
    }
}