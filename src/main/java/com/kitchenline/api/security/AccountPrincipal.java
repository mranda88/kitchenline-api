package com.kitchenline.api.security;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/** Who is logged in. Spring Security keeps one of these in the session. */
public class AccountPrincipal implements UserDetails, CredentialsContainer {

    private final Long id;
    private final String email;
    private String passwordHash;

    public AccountPrincipal(Long id, String email, String passwordHash) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public Long getId() { return id; }

    @Override
    public String getUsername() { return email; }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    /** Spring calls this right after a successful login, so the hash isn't kept in the session. */
    @Override
    public void eraseCredentials() {
        passwordHash = null;
    }
}