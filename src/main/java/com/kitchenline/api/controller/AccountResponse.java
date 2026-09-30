package com.kitchenline.api.controller;

import com.kitchenline.api.domain.AppUser;
import com.kitchenline.api.domain.SubscriptionStatus;

public record AccountResponse(Long id, String email, SubscriptionStatus subscriptionStatus) {

    public static AccountResponse from(AppUser user) {
        return new AccountResponse(user.getId(), user.getEmail(), user.getSubscriptionStatus());
    }
}