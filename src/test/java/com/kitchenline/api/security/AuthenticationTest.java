package com.kitchenline.api.security;

import com.kitchenline.api.domain.AppUser;
import com.kitchenline.api.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthenticationTest {

    @Autowired AccountService accounts;
    @Autowired AuthenticationManager authenticationManager;

    private Authentication login(String email, String password) {
        return authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, password));
    }

    @Test
    void correctPasswordLogsInAsTheRightUser() {
        AppUser user = accounts.signUp("login@example.com", "PickleBall2026");

        Authentication auth = login("login@example.com", "PickleBall2026");

        assertTrue(auth.isAuthenticated());
        assertEquals(user.getId(), ((AccountPrincipal) auth.getPrincipal()).getId());
    }

    @Test
    void emailCapitalsDontMatterAtLogin() {
        accounts.signUp("caps@example.com", "PickleBall2026");
        assertTrue(login("CAPS@Example.com", "PickleBall2026").isAuthenticated());
    }

    @Test
    void passwordHashIsClearedAfterLogin() {
        accounts.signUp("erase@example.com", "PickleBall2026");
        Authentication auth = login("erase@example.com", "PickleBall2026");
        assertNull(((AccountPrincipal) auth.getPrincipal()).getPassword());
    }

    @Test
    void wrongPasswordIsRejected() {
        accounts.signUp("wrong@example.com", "PickleBall2026");
        assertThrows(BadCredentialsException.class, () -> login("wrong@example.com", "NotMyPassword1"));
    }

    @Test
    void unknownEmailGetsTheSameErrorAsWrongPassword() {
        assertThrows(BadCredentialsException.class, () -> login("nobody@example.com", "PickleBall2026"));
    }
}