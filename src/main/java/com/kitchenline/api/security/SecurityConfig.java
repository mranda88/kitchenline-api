package com.kitchenline.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Who can reach what
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health", "/api/auth/signup", "/error").permitAll()
                        .anyRequest().authenticated())
                // CSRF protection is set up properly for the front end in 4c.
                // Sign-up doesn't need it, since there's no logged-in session to hijack yet.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/signup"))
                // Not logged in = a clean 401 response instead of a login page
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt by default. Stored hashes start with {bcrypt} so the
        // algorithm can be upgraded later without breaking old accounts.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}