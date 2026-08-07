package mt.client.manager;

import mt.cache.ClientAchievementCache;
import mt.server.AchievementDefinition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WellRestedClientStateTest {

    @AfterEach
    void clearState() {
        WellRestedClientState.reset();
        ClientAchievementCache.clear();
    }

    @Test
    void updateStoresEveryField() {
        WellRestedClientState.update(true, 4, 1200, 2400, true, true);

        assertTrue(WellRestedClientState.isActive());
        assertEquals(4, WellRestedClientState.getLevel());
        assertEquals(1200, WellRestedClientState.getTicksRemaining());
        assertEquals(2400, WellRestedClientState.getTotalTicks());
        assertTrue(WellRestedClientState.isNightmareMode());
        assertTrue(WellRestedClientState.isMvp());
    }

    @Test
    void resetClearsEveryField() {
        WellRestedClientState.update(true, 5, 999, 1000, true, true);

        WellRestedClientState.reset();

        assertFalse(WellRestedClientState.isActive());
        assertEquals(0, WellRestedClientState.getLevel());
        assertEquals(0, WellRestedClientState.getTicksRemaining());
        assertEquals(0, WellRestedClientState.getTotalTicks());
        assertFalse(WellRestedClientState.isNightmareMode());
        assertFalse(WellRestedClientState.isMvp());
    }

    @Test
    void resetIsIdempotent() {
        WellRestedClientState.update(true, 5, 999, 1000, true, true);

        for (int i = 0; i < 100; i++) {
            WellRestedClientState.reset();
        }

        assertFalse(WellRestedClientState.isActive());
        assertEquals(0, WellRestedClientState.getLevel());
    }

    @Test
    void stateFromAPreviousServerNeverLeaksAfterReset() {
        WellRestedClientState.update(true, 3, 500, 1000, true, true);
        WellRestedClientState.reset();
        WellRestedClientState.update(true, 1, 100, 200, false, false);

        assertEquals(1, WellRestedClientState.getLevel());
        assertFalse(WellRestedClientState.isNightmareMode());
        assertFalse(WellRestedClientState.isMvp());
    }

    @Test
    void progressNeverExceedsOneForAnyReportedTickPair() {
        int[][] pairs = {{0, 0}, {0, 100}, {50, 100}, {100, 100}, {150, 100}, {1, 1}};
        for (int[] pair : pairs) {
            WellRestedClientState.update(true, 1, pair[0], pair[1], false, false);

            int remaining = WellRestedClientState.getTicksRemaining();
            int total = WellRestedClientState.getTotalTicks();
            float progress = total <= 0 ? 0.0f : Math.min(1.0f, remaining / (float) total);

            assertTrue(progress >= 0.0f && progress <= 1.0f,
                    "progress out of range for " + pair[0] + "/" + pair[1]);
        }
    }

    @Test
    void achievementCacheStartsEmptyAndClears() {
        assertTrue(ClientAchievementCache.get().isEmpty());

        ClientAchievementCache.apply(List.of(new AchievementDefinition()));
        assertEquals(1, ClientAchievementCache.get().size());

        ClientAchievementCache.clear();
        assertTrue(ClientAchievementCache.get().isEmpty());
    }

    @Test
    void achievementCacheIsDefensivelyCopied() {
        List<AchievementDefinition> source = new ArrayList<>();
        source.add(new AchievementDefinition());

        ClientAchievementCache.apply(source);
        List<AchievementDefinition> cached = ClientAchievementCache.get();
        source.add(new AchievementDefinition());

        assertNotSame(source, cached);
        assertEquals(1, cached.size(), "cache changed when the source list was mutated");
    }

    @Test
    void cachedAchievementsAreImmutable() {
        ClientAchievementCache.apply(List.of(new AchievementDefinition()));

        assertThrows(UnsupportedOperationException.class,
                () -> ClientAchievementCache.get().add(new AchievementDefinition()));
    }

    @Test
    void rapidUpdatesLeaveConsistentState() {
        for (int i = 0; i < 500_000; i++) {
            WellRestedClientState.update(i % 2 == 0, i % 6, i, i + 1, i % 3 == 0, i % 5 == 0);
        }

        assertEquals(499_999, WellRestedClientState.getTicksRemaining());
        assertEquals(500_000, WellRestedClientState.getTotalTicks());
        assertFalse(WellRestedClientState.isActive());
    }
}
