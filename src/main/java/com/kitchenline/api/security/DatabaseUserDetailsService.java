package com.kitchenline.api.security;

import com.kitchenline.api.domain.AppUser;
import com.kitchenline.api.repository.AppUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Tells Spring Security how to find a user by email in our database. */
@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final AppUserRepository users;

    public DatabaseUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String clean = email == null ? "" : email.trim().toLowerCase();
        AppUser user = users.findByEmail(clean)
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + clean));
        return new AccountPrincipal(user.getId(), user.getEmail(), user.getPasswordHash());
    }
}