package mt.client.ui.summary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatsCountUpTest {

    private static final int[] VALUES = {120, 2431, 14, 1, 306, 0};

    @Test
    void startsAtZeroAndEndsAtTheRealValues() {
        assertArrayEquals(new int[VALUES.length], StatsPanelRenderer.counted(VALUES, 0.0f));
        assertArrayEquals(new int[VALUES.length], StatsPanelRenderer.counted(VALUES, -1.0f));
        assertArrayEquals(VALUES, StatsPanelRenderer.counted(VALUES, 1.0f));
        assertArrayEquals(VALUES, StatsPanelRenderer.counted(VALUES, 2.0f));
    }

    @Test
    void countsUpWithoutOvershootingOrGoingBack() {
        int[] previous = new int[VALUES.length];
        for (int step = 0; step <= 100; step++) {
            int[] shown = StatsPanelRenderer.counted(VALUES, step / 100.0f);
            for (int i = 0; i < VALUES.length; i++) {
                assertTrue(shown[i] >= previous[i], "value " + i + " went back at step " + step);
                assertTrue(shown[i] <= VALUES[i], "value " + i + " overshot at step " + step);
            }
            previous = shown;
        }
    }

    @Test
    void largeValuesDoNotOverflow() {
        int[] large = {Integer.MAX_VALUE};
        int shown = StatsPanelRenderer.counted(large, 0.99f)[0];
        assertTrue(shown > 0, "large value overflowed: " + shown);
    }
}
