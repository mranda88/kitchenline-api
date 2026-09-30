package com.kitchenline.api.engine;

public record Intensity(
        int tier,              // 1-5
        String label,          // "Foundation" ... "Elite"
        boolean jumpsAllowed   // whether plyometric exercises can be included
) {}
