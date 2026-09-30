package com.kitchenline.api.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TierCalculatorTest {

    private final TierCalculator calculator = new TierCalculator();

    // Helper: a profile where only age and skill matter
    private PlayerProfile player(int age, SkillLevel skill) {
        return new PlayerProfile(age, Gender.UNSPECIFIED, skill, 2, 3, 3);
    }

    @Test
    void youngExpertIsEliteWithJumps() {
        Intensity result = calculator.calculate(player(25, SkillLevel.EXPERT), 1);
        assertEquals(5, result.tier());
        assertEquals("Elite", result.label());
        assertTrue(result.jumpsAllowed());
    }

    @Test
    void intermediateAt34IsCompete() {
        Intensity result = calculator.calculate(player(34, SkillLevel.INTERMEDIATE), 1);
        assertEquals(3, result.tier());
        assertFalse(result.jumpsAllowed());
    }

    @Test
    void playersInTheir60sAreCappedAtTier4WithNoJumps() {
        Intensity result = calculator.calculate(player(62, SkillLevel.EXPERT), 1);
        assertEquals(4, result.tier());
        assertFalse(result.jumpsAllowed());
    }

    @Test
    void players70AndOverAreCappedAtTier3() {
        assertEquals(3, calculator.calculate(player(75, SkillLevel.EXPERT), 1).tier());
    }

    @Test
    void tierNeverDropsBelow1() {
        assertEquals(1, calculator.calculate(player(85, SkillLevel.BEGINNER), 1).tier());
    }

    @Test
    void tierRisesOneLevelEveryTwoMonths() {
        PlayerProfile p = player(34, SkillLevel.INTERMEDIATE);
        assertEquals(3, calculator.calculate(p, 1).tier());
        assertEquals(3, calculator.calculate(p, 2).tier());
        assertEquals(4, calculator.calculate(p, 3).tier());
        assertEquals(5, calculator.calculate(p, 5).tier());
    }

    @Test
    void monthlyProgressionNeverPassesTheAgeCap() {
        assertEquals(4, calculator.calculate(player(65, SkillLevel.EXPERT), 12).tier());
    }

    @Test
    void monthZeroIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> calculator.calculate(player(34, SkillLevel.INTERMEDIATE), 0));
    }
}