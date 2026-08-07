package mt.client.render;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NightmareFieldTest {

    private static final float VERTICAL_PERIOD = 5200f;
    private static final float HORIZONTAL_PERIOD = 6700f;

    @Test
    void closureStaysWithinTheUnitRange() {
        for (float elapsed = 0f; elapsed < 600_000f; elapsed += 3f) {
            float vertical = NightmareField.verticalClosure(elapsed);
            float horizontal = NightmareField.horizontalClosure(elapsed);
            assertTrue(vertical >= 0f && vertical <= 1f,
                    "vertical closure out of range at " + elapsed + ": " + vertical);
            assertTrue(horizontal >= 0f && horizontal <= 1f,
                    "horizontal closure out of range at " + elapsed + ": " + horizontal);
        }
    }

    @Test
    void closureIsFiniteForALongSleep() {
        for (float elapsed = 0f; elapsed < 3_600_000f; elapsed += 250f) {
            assertTrue(Float.isFinite(NightmareField.verticalClosure(elapsed)));
            assertTrue(Float.isFinite(NightmareField.horizontalClosure(elapsed)));
        }
    }

    @Test
    void closureReachesBothExtremes() {
        float widest = 1f;
        float narrowest = 0f;
        for (float elapsed = 0f; elapsed < VERTICAL_PERIOD; elapsed += 1f) {
            float closure = NightmareField.verticalClosure(elapsed);
            widest = Math.min(widest, closure);
            narrowest = Math.max(narrowest, closure);
        }

        assertEquals(0f, widest, 1e-3f, "the overlay never fully opens up");
        assertEquals(1f, narrowest, 1e-3f, "the overlay never fully closes in");
    }

    @Test
    void closureMovesSmoothlyBetweenFrames() {
        float previous = NightmareField.verticalClosure(0f);
        for (float elapsed = 16f; elapsed < 30_000f; elapsed += 16f) {
            float current = NightmareField.verticalClosure(elapsed);
            assertTrue(Math.abs(current - previous) < 0.02f,
                    "closure jumped by " + Math.abs(current - previous) + " at " + elapsed);
            previous = current;
        }
    }

    @Test
    void closureActuallyChangesOverASecond() {
        float start = NightmareField.verticalClosure(0f);
        float later = NightmareField.verticalClosure(1_000f);

        assertTrue(Math.abs(later - start) > 0.05f,
                "the overlay is frozen, nothing would move on screen");
    }

    @Test
    void eachAxisRepeatsOnItsOwnPeriod() {
        for (float offset = 0f; offset < 1_000f; offset += 37f) {
            assertEquals(NightmareField.verticalClosure(offset),
                    NightmareField.verticalClosure(offset + VERTICAL_PERIOD), 1e-3f,
                    "the vertical rhythm drifted at " + offset);
            assertEquals(NightmareField.horizontalClosure(offset),
                    NightmareField.horizontalClosure(offset + HORIZONTAL_PERIOD), 1e-3f,
                    "the horizontal rhythm drifted at " + offset);
        }
    }

    @Test
    void theTwoAxesDoNotBreatheInLockstep() {
        int identical = 0;
        int samples = 0;
        for (float elapsed = 0f; elapsed < 20_000f; elapsed += 20f) {
            samples++;
            if (Math.abs(NightmareField.verticalClosure(elapsed)
                    - NightmareField.horizontalClosure(elapsed)) < 0.01f) {
                identical++;
            }
        }

        assertTrue(identical < samples / 10,
                "both axes move together, the effect would look mechanical");
    }

    @Test
    void bandSizesStayInsideTheScreen() {
        for (float elapsed = 0f; elapsed < 60_000f; elapsed += 5f) {
            float height = NightmareField.lerp(0.24f, 0.44f, NightmareField.verticalClosure(elapsed));
            float width = NightmareField.lerp(0.16f, 0.34f, NightmareField.horizontalClosure(elapsed));

            assertTrue(height >= 0.24f && height <= 0.44f, "band height out of bounds: " + height);
            assertTrue(width >= 0.16f && width <= 0.34f, "band width out of bounds: " + width);
            assertTrue(height * 2f < 1f, "top and bottom bands would overlap");
            assertTrue(width * 2f < 1f, "left and right bands would overlap");
        }
    }

    @Test
    void interpolationHitsBothEnds() {
        assertEquals(10f, NightmareField.lerp(10f, 20f, 0f));
        assertEquals(20f, NightmareField.lerp(10f, 20f, 1f));
        assertEquals(15f, NightmareField.lerp(10f, 20f, 0.5f));
    }

    @Test
    void negativeElapsedTimeIsHandled() {
        assertTrue(Float.isFinite(NightmareField.verticalClosure(-5_000f)));
        assertTrue(Float.isFinite(NightmareField.horizontalClosure(-5_000f)));
        assertTrue(NightmareField.verticalClosure(-5_000f) >= 0f);
        assertTrue(NightmareField.verticalClosure(-5_000f) <= 1f);
    }
}
