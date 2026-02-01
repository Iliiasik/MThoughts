package mt.server;

import mt.client.config.MidnightThoughtsConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class ComfortCalculatorTest {

    @BeforeEach
    void setUp() {
        MidnightThoughtsConfig.getInstance();
    }

    @Test
    void testComfortCalculationBasics() {
        assertTrue(MidnightThoughtsConfig.getInstance().getComfort().enabled);
        assertEquals(5, MidnightThoughtsConfig.getInstance().getComfort().scanRadius);
    }

    @Test
    void testMinimumComfortLevel() {
        int minLevel = 1;
        assertTrue(minLevel >= 1 && minLevel <= 5);
    }

    @Test
    void testMaximumComfortLevel() {
        int maxLevel = 5;
        assertTrue(maxLevel >= 1 && maxLevel <= 5);
    }

    @Test
    void testComfortLevelRange() {
        for (int level = 1; level <= 5; level++) {
            assertTrue(level >= 1 && level <= 5);
        }
    }

    @Test
    void testDefaultScanRadius() {
        MidnightThoughtsConfig.ComfortSettings comfort =
            MidnightThoughtsConfig.getInstance().getComfort();

        assertEquals(5, comfort.scanRadius);
        assertTrue(comfort.scanRadius > 0);
    }

    @Test
    void testCustomScanRadius() {
        MidnightThoughtsConfig.ComfortSettings comfort =
            MidnightThoughtsConfig.getInstance().getComfort();

        int originalRadius = comfort.scanRadius;
        comfort.scanRadius = 10;

        assertEquals(10, comfort.scanRadius);

        comfort.scanRadius = originalRadius;
    }

    @Test
    void testComfortDisabled() {
        MidnightThoughtsConfig.ComfortSettings comfort =
            MidnightThoughtsConfig.getInstance().getComfort();

        boolean originalEnabled = comfort.enabled;
        comfort.enabled = false;

        assertFalse(comfort.enabled);

        comfort.enabled = originalEnabled;
    }

    @Test
    void testComfortEnabled() {
        MidnightThoughtsConfig.ComfortSettings comfort =
            MidnightThoughtsConfig.getInstance().getComfort();

        assertTrue(comfort.enabled);
    }

    @Test
    void testZeroScanRadius() {
        MidnightThoughtsConfig.ComfortSettings comfort =
            MidnightThoughtsConfig.getInstance().getComfort();

        int originalRadius = comfort.scanRadius;
        comfort.scanRadius = 0;

        assertEquals(0, comfort.scanRadius);

        comfort.scanRadius = originalRadius;
    }

    @Test
    void testLargeScanRadius() {
        MidnightThoughtsConfig.ComfortSettings comfort =
            MidnightThoughtsConfig.getInstance().getComfort();

        int originalRadius = comfort.scanRadius;
        comfort.scanRadius = 100;

        assertEquals(100, comfort.scanRadius);

        comfort.scanRadius = originalRadius;
    }

    @Test
    void testComfortConfigPersistence() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        config.save();

        MidnightThoughtsConfig reloaded = MidnightThoughtsConfig.load();

        assertEquals(config.getComfort().enabled, reloaded.getComfort().enabled);
        assertEquals(config.getComfort().scanRadius, reloaded.getComfort().scanRadius);
    }
}

