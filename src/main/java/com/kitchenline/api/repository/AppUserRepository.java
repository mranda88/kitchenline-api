package com.kitchenline.api.repository;

import com.kitchenline.api.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // Used at login: find the account for this email
    Optional<AppUser> findByEmail(String email);

    // Used at sign-up: stop two accounts using one email
    boolean existsByEmail(String email);
}