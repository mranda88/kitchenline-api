package com.kitchenline.api.service;

import com.kitchenline.api.domain.AppUser;
import com.kitchenline.api.domain.SubscriptionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AccountServiceTest {

    @Autowired AccountService accounts;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void passwordIsStoredAsAHashNotPlainText() {
        AppUser user = accounts.signUp("hash@example.com", "PickleBall2026");

        assertNotEquals("PickleBall2026", user.getPasswordHash());
        assertTrue(passwordEncoder.matches("PickleBall2026", user.getPasswordHash()));
        assertFalse(passwordEncoder.matches("wrong-password", user.getPasswordHash()));
    }

    @Test
    void newAccountsStartOnTheFreeTrial() {
        AppUser user = accounts.signUp("trial@example.com", "PickleBall2026");
        assertEquals(SubscriptionStatus.TRIAL, user.getSubscriptionStatus());
    }

    @Test
    void emailIsTrimmedAndLowercased() {
        AppUser user = accounts.signUp("  Mixed.Case@Example.COM ", "PickleBall2026");
        assertEquals("mixed.case@example.com", user.getEmail());
    }

    @Test
    void duplicateEmailIsRejectedEvenWithDifferentCapitals() {
        accounts.signUp("dupe@example.com", "PickleBall2026");
        assertThrows(IllegalStateException.class,
                () -> accounts.signUp("DUPE@example.com", "AnotherPass99"));
    }

    @Test
    void shortPasswordIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> accounts.signUp("short@example.com", "dink"));
    }

    @Test
    void invalidEmailsAreRejected() {
        for (String bad : new String[]{"not-an-email", "a@b", "@example.com", "a b@example.com", "a@.com"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> accounts.signUp(bad, "PickleBall2026"),
                    bad + " should be rejected");
        }
    }
}