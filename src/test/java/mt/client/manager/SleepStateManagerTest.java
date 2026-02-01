package mt.client.manager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

class SleepStateManagerTest {

    private SleepStateManager manager;

    @BeforeEach
    void setUp() {
        manager = new SleepStateManager();
    }

    @Test
    void testInitialState() {
        assertFalse(manager.isSleeping());
        assertFalse(manager.justStartedSleeping());
        assertFalse(manager.justStoppedSleeping());
    }

    @Test
    void testJustStartedSleeping() {
        manager = new SleepStateManager();

        assertFalse(manager.isSleeping());
        assertFalse(manager.justStartedSleeping());
    }

    @Test
    void testMultipleTicksWhileSleeping() {
        manager = new SleepStateManager();

        assertFalse(manager.justStartedSleeping());
    }

    @Test
    void testStateTransitions() {
        assertFalse(manager.isSleeping());
        assertFalse(manager.justStartedSleeping());
        assertFalse(manager.justStoppedSleeping());
    }

    @Test
    void testRepeatedSleepCycles() {
        for (int i = 0; i < 10; i++) {
            assertFalse(manager.isSleeping());
        }
    }

    @Test
    void testRapidStateChanges() {
        for (int i = 0; i < 100; i++) {
            assertFalse(manager.isSleeping());
        }
    }

    @Test
    void testNoPlayerState() {
        assertFalse(manager.isSleeping());
        assertFalse(manager.justStartedSleeping());
        assertFalse(manager.justStoppedSleeping());
    }
}

