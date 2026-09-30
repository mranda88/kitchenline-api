package com.kitchenline.api.controller;

import org.springframework.security.core.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    /** Wrong email or password → 401. Same message either way, so attackers can't tell which emails exist. */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> unauthorized(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Email or password is incorrect."));
    }
    /** Bad input, e.g. a short password or week 7 → 400 */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    /** Valid request that isn't allowed right now, e.g. email taken → 409 */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> conflict(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    /** Broken JSON or an unknown value like "gender":"BANANA" → 400 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> unreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "The request body is missing or contains an invalid value."));
    }
}