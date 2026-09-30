package com.kitchenline.api.domain;

public enum SubscriptionStatus {
    TRIAL,      // signed up, hasn't paid: week 1 only
    ACTIVE,     // paying member: full program
    PAST_DUE,   // card payment failed; Stripe is retrying
    CANCELED    // membership ended
}