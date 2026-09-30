package com.kitchenline.api.service;

import com.kitchenline.api.domain.AppUser;
import com.kitchenline.api.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 64;

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    /** Creates a new account on the free trial. The password is stored only as a hash. */
    public AppUser signUp(String email, String password) {
        String cleanEmail = normalizeEmail(email);
        validatePassword(password);

        if (users.existsByEmail(cleanEmail)) {
            throw new IllegalStateException("An account with this email already exists.");
        }
        return users.save(new AppUser(cleanEmail, passwordEncoder.encode(password)));
    }

    /** Loads the current account, e.g. to show its subscription status. */
    @Transactional(readOnly = true)
    public AppUser getAccount(Long userId) {
        return users.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user with id " + userId));
    }

    /** Trims spaces, lowercases, and checks the email looks real (name@domain.tld). */
    static String normalizeEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email is required.");
        }
        String e = email.trim().toLowerCase();
        int at = e.indexOf('@');
        boolean valid = at >= 1
                && e.lastIndexOf('.') >= at + 2
                && !e.endsWith(".")
                && !e.contains(" ")
                && e.length() <= 255;
        if (!valid) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        return e;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must be 64 characters or fewer.");
        }
    }
}