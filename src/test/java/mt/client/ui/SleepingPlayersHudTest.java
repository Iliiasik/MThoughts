package mt.client.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SleepingPlayersHudTest {

    private static final float FADE_DURATION_MS = 160.0f;

    private static float fadeOverFrames(int frameCount, float frameMs) {
        float alpha = 0f;
        long now = 1_000_000L;
        long previous = 0L;
        for (int i = 0; i < frameCount; i++) {
            alpha = Math.min(1.0f, alpha + SleepingPlayersHud.fadeStep(now, previous));
            previous = now;
            now += (long) frameMs;
        }
        return alpha;
    }

    private static int framesUntilOpaque(float frameMs) {
        float alpha = 0f;
        long now = 1_000_000L;
        long previous = 0L;
        int frames = 0;
        while (alpha < 1.0f && frames < 10_000) {
            alpha = Math.min(1.0f, alpha + SleepingPlayersHud.fadeStep(now, previous));
            previous = now;
            now += (long) frameMs;
            frames++;
        }
        return frames;
    }

    @Test
    void theFadeTakesTheSameTimeAtAnyFrameRate() {
        float at30 = framesUntilOpaque(1000f / 30f) * (1000f / 30f);
        float at60 = framesUntilOpaque(1000f / 60f) * (1000f / 60f);
        float at144 = framesUntilOpaque(1000f / 144f) * (1000f / 144f);

        assertEquals(FADE_DURATION_MS, at30, 50f, "fade duration drifted at 30 fps");
        assertEquals(FADE_DURATION_MS, at60, 50f, "fade duration drifted at 60 fps");
        assertEquals(FADE_DURATION_MS, at144, 50f, "fade duration drifted at 144 fps");
    }

    @Test
    void aHigherFrameRateNeedsMoreFramesForTheSameFade() {
        int at30 = framesUntilOpaque(1000f / 30f);
        int at144 = framesUntilOpaque(1000f / 144f);

        assertTrue(at144 > at30,
                "the fade is still counted in frames rather than in time");
    }

    @Test
    void theFirstFrameNeverJumps() {
        float step = SleepingPlayersHud.fadeStep(1_000_000L, 0L);

        assertTrue(step > 0f && step <= 0.2f, "unexpected first step: " + step);
    }

    @Test
    void aLagSpikeDoesNotSnapTheHudOpen() {
        float step = SleepingPlayersHud.fadeStep(1_005_000L, 1_000_000L);

        assertTrue(step <= 100.0f / FADE_DURATION_MS + 1e-6f,
                "a five second stall produced a step of " + step);
    }

    @Test
    void aClockGoingBackwardsNeverProducesANegativeStep() {
        assertTrue(SleepingPlayersHud.fadeStep(1_000_000L, 1_005_000L) >= 0f);
    }

    @Test
    void theFadeIsMonotonicAndReachesFullOpacity() {
        float previous = -1f;
        float alpha = 0f;
        long now = 1_000_000L;
        long last = 0L;
        for (int i = 0; i < 60; i++) {
            alpha = Math.min(1.0f, alpha + SleepingPlayersHud.fadeStep(now, last));
            assertTrue(alpha >= previous, "the fade went backwards at frame " + i);
            assertTrue(alpha <= 1.0f, "the fade overshot at frame " + i);
            previous = alpha;
            last = now;
            now += 16L;
        }
        assertEquals(1.0f, alpha, 1e-6f, "the hud never became fully visible");
    }

    @Test
    void halfTheDurationGivesRoughlyHalfTheOpacity() {
        float alpha = fadeOverFrames(5, 16f);

        assertTrue(alpha > 0.3f && alpha < 0.7f, "unexpected midpoint opacity: " + alpha);
    }
}
