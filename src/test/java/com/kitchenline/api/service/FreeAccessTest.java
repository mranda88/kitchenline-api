package com.kitchenline.api.service;

import com.kitchenline.api.domain.AppUser;
import com.kitchenline.api.domain.ProgramMonth;
import com.kitchenline.api.engine.Gender;
import com.kitchenline.api.engine.PlayerProfile;
import com.kitchenline.api.engine.SkillLevel;
import com.kitchenline.api.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/** With the paywall off (the default), every account gets the full program. */
@SpringBootTest
@Transactional
class FreeAccessTest {

    @Autowired TrainingService service;
    @Autowired AppUserRepository users;

    private final PlayerProfile answers =
            new PlayerProfile(34, Gender.FEMALE, SkillLevel.INTERMEDIATE, 2, 3, 3);

    private Long newUser() {
        // New accounts start as TRIAL, which no longer limits anything
        return users.save(new AppUser("free" + System.nanoTime() + "@example.com", "x")).getId();
    }

    @Test
    void everyoneGetsAllFourWeeks() {
        Long id = newUser();
        service.saveProfile(id, answers);

        ProgramView view = service.getCurrentProgram(id);
        assertEquals(4, view.unlockedWeeks());
        assertEquals(4, view.program().weeks().size());
    }

    @Test
    void anyoneCanLogWeek4() {
        Long id = newUser();
        service.saveProfile(id, answers);

        service.markDone(id, 4, "C");
        assertEquals(1, service.getCurrentProgram(id).completedCount());
    }

    @Test
    void anyoneCanStartTheNextMonthAt75Percent() {
        Long id = newUser();
        service.saveProfile(id, answers);
        String[] days = {"A", "B", "C"};
        for (int i = 0; i < 9; i++) {
            service.markDone(id, i / 3 + 1, days[i % 3]);
        }

        ProgramMonth month2 = service.startNextMonth(id);
        assertEquals(2, month2.getMonthNumber());
    }

    @Test
    void the75PercentRuleStillApplies() {
        Long id = newUser();
        service.saveProfile(id, answers);

        assertThrows(IllegalStateException.class, () -> service.startNextMonth(id));
    }
}