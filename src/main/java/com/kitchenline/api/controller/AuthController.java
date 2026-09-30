package com.kitchenline.api.controller;

import com.kitchenline.api.domain.AppUser;
import com.kitchenline.api.security.AccountPrincipal;
import com.kitchenline.api.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accounts;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AccountService accounts,
                          AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.accounts = accounts;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse signUp(@RequestBody SignUpRequest request) {
        AppUser user = accounts.signUp(request.email(), request.password());
        return AccountResponse.from(user);
    }

    @PostMapping("/login")
    public AccountResponse login(@RequestBody LoginRequest body,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        // 1. Check the email and password (throws an error if wrong)
        String email = body.email() == null ? "" : body.email().trim().toLowerCase();
        Authentication auth = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(email, body.password()));

        // 2. Give the browser a fresh session ID (blocks "session fixation" attacks)
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        // 3. Remember this user in the session for future requests
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        AccountPrincipal principal = (AccountPrincipal) auth.getPrincipal();
        return AccountResponse.from(accounts.getAccount(principal.getId()));
    }

    /**
     * "Who am I?" The front end calls this on page load to see if someone is logged in.
     */
    @GetMapping("/me")
    public AccountResponse me(@AuthenticationPrincipal AccountPrincipal principal) {
        return AccountResponse.from(accounts.getAccount(principal.getId()));
    }
}