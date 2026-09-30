package com.kitchenline.api.controller;

import com.kitchenline.api.domain.AppUser;
import com.kitchenline.api.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AccountService accounts;

    public AuthController(AccountService accounts) {
        this.accounts = accounts;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse signUp(@RequestBody SignUpRequest request) {
        AppUser user = accounts.signUp(request.email(), request.password());
        return AccountResponse.from(user);
    }
}