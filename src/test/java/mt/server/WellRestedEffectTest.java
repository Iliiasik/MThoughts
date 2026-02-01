package mt.server;

import mt.client.config.MidnightThoughtsConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class WellRestedEffectTest {

    @BeforeEach
    void setUp() {
        MidnightThoughtsConfig.getInstance();
    }

    @Test
    void testGetDurationForLevel1() {
        int duration = WellRestedEffect.getDurationForLevel(1);
        assertEquals(3 * 60 * 20, duration);
    }

    @Test
    void testGetDurationForLevel2() {
        int duration = WellRestedEffect.getDurationForLevel(2);
        assertEquals(5 * 60 * 20, duration);
    }

    @Test
    void testGetDurationForLevel3() {
        int duration = WellRestedEffect.getDurationForLevel(3);
        assertEquals(7 * 60 * 20, duration);
    }

    @Test
    void testGetDurationForLevel4() {
        int duration = WellRestedEffect.getDurationForLevel(4);
        assertEquals(10 * 60 * 20, duration);
    }

    @Test
    void testGetDurationForLevel5() {
        int duration = WellRestedEffect.getDurationForLevel(5);
        assertEquals(15 * 60 * 20, duration);
    }

    @Test
    void testDurationIncreasesWithLevel() {
        int level1 = WellRestedEffect.getDurationForLevel(1);
        int level2 = WellRestedEffect.getDurationForLevel(2);
        int level3 = WellRestedEffect.getDurationForLevel(3);
        int level4 = WellRestedEffect.getDurationForLevel(4);
        int level5 = WellRestedEffect.getDurationForLevel(5);

        assertTrue(level1 < level2);
        assertTrue(level2 < level3);
        assertTrue(level3 < level4);
        assertTrue(level4 < level5);
    }

    @Test
    void testCustomConfigValues() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        config.getWellRested().levels.put("level1",
            new MidnightThoughtsConfig.WellRestedLevel(10, 5.0, 1.0, 0.1f, 10.0f));

        int duration = WellRestedEffect.getDurationForLevel(1);
        assertEquals(10 * 60 * 20, duration);

        MidnightThoughtsConfig.WellRestedLevel level = config.getWellRested().getLevel(1);
        assertEquals(10, level.durationMinutes);
        assertEquals(5.0, level.healthBonus);
        assertEquals(1.0, level.luckBonus);
        assertEquals(0.1f, level.exhaustionReduction);
        assertEquals(10.0f, level.instantHeal);

        config.getWellRested().levels.put("level1",
            new MidnightThoughtsConfig.WellRestedLevel(3, 2.0, 0.25, 0.005f, 2.0f));
    }

    @Test
    void testInvalidLevelDefaultsToLevel1() {
        int duration = WellRestedEffect.getDurationForLevel(999);
        int level1Duration = WellRestedEffect.getDurationForLevel(1);
        assertEquals(level1Duration, duration);
    }

    @Test
    void testZeroLevelDefaultsToLevel1() {
        int duration = WellRestedEffect.getDurationForLevel(0);
        int level1Duration = WellRestedEffect.getDurationForLevel(1);
        assertEquals(level1Duration, duration);
    }

    @Test
    void testNegativeLevelDefaultsToLevel1() {
        int duration = WellRestedEffect.getDurationForLevel(-5);
        int level1Duration = WellRestedEffect.getDurationForLevel(1);
        assertEquals(level1Duration, duration);
    }

    @Test
    void testAllLevelsHaveValidDurations() {
        for (int level = 1; level <= 5; level++) {
            int duration = WellRestedEffect.getDurationForLevel(level);
            assertTrue(duration > 0, "Level " + level + " should have positive duration");
            assertTrue(duration % 20 == 0, "Level " + level + " duration should be in ticks (divisible by 20)");
        }
    }

    @Test
    void testEffectProperties() {
        MidnightThoughtsConfig.WellRestedLevel level1 =
            MidnightThoughtsConfig.getInstance().getWellRested().getLevel(1);

        assertEquals(2.0, level1.healthBonus);
        assertEquals(0.25, level1.luckBonus);
        assertEquals(0.005f, level1.exhaustionReduction);
        assertEquals(2.0f, level1.instantHeal);
    }

    @Test
    void testHigherLevelsHaveStrongerEffects() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();

        MidnightThoughtsConfig.WellRestedLevel level1 = config.getWellRested().getLevel(1);
        MidnightThoughtsConfig.WellRestedLevel level5 = config.getWellRested().getLevel(5);

        assertTrue(level5.durationMinutes > level1.durationMinutes);
        assertTrue(level5.exhaustionReduction > level1.exhaustionReduction);
        assertTrue(level5.instantHeal >= level1.instantHeal);
    }
}

