package mt.server;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DailyPlayerStatsTest {

    @Test
    void testDailyDeltaCreation() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            100, 5000, 20, 3, 150
        );

        assertEquals(100, delta.blocksDestroyed());
        assertEquals(5000, delta.distanceWalked());
        assertEquals(20, delta.mobsKilled());
        assertEquals(3, delta.deaths());
        assertEquals(150, delta.jumps());
    }

    @Test
    void testZeroDelta() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            0, 0, 0, 0, 0
        );

        assertEquals(0, delta.blocksDestroyed());
        assertEquals(0, delta.distanceWalked());
        assertEquals(0, delta.mobsKilled());
        assertEquals(0, delta.deaths());
        assertEquals(0, delta.jumps());
    }

    @Test
    void testLargeValues() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE,
            Integer.MAX_VALUE
        );

        assertEquals(Integer.MAX_VALUE, delta.blocksDestroyed());
        assertEquals(Integer.MAX_VALUE, delta.distanceWalked());
        assertEquals(Integer.MAX_VALUE, delta.mobsKilled());
        assertEquals(Integer.MAX_VALUE, delta.deaths());
        assertEquals(Integer.MAX_VALUE, delta.jumps());
    }

    @Test
    void testDeltaEquality() {
        DailyPlayerStats.DailyDelta delta1 = new DailyPlayerStats.DailyDelta(100, 5000, 20, 3, 150);
        DailyPlayerStats.DailyDelta delta2 = new DailyPlayerStats.DailyDelta(100, 5000, 20, 3, 150);

        assertEquals(delta1, delta2);
        assertEquals(delta1.hashCode(), delta2.hashCode());
    }

    @Test
    void testDeltaInequality() {
        DailyPlayerStats.DailyDelta delta1 = new DailyPlayerStats.DailyDelta(100, 5000, 20, 3, 150);
        DailyPlayerStats.DailyDelta delta2 = new DailyPlayerStats.DailyDelta(200, 5000, 20, 3, 150);

        assertNotEquals(delta1, delta2);
    }

    @Test
    void testNegativeValuesNotAllowed() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            -10, -100, -5, -1, -50
        );

        assertTrue(delta.blocksDestroyed() < 0);
        assertTrue(delta.distanceWalked() < 0);
        assertTrue(delta.mobsKilled() < 0);
        assertTrue(delta.deaths() < 0);
        assertTrue(delta.jumps() < 0);
    }

    @Test
    void testTypicalGameplayValues() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            523, 15234, 37, 2, 421
        );

        assertTrue(delta.blocksDestroyed() > 0);
        assertTrue(delta.distanceWalked() > 0);
        assertTrue(delta.mobsKilled() > 0);
        assertTrue(delta.deaths() >= 0);
        assertTrue(delta.jumps() > 0);
    }

    @Test
    void testDistanceConversion() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            0, 10000, 0, 0, 0
        );

        int blocksWalked = delta.distanceWalked() / 100;
        assertEquals(100, blocksWalked);
    }

    @Test
    void testPacifistPlaythrough() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            50, 100000, 0, 0, 200
        );

        assertEquals(0, delta.mobsKilled());
        assertEquals(0, delta.deaths());
        assertTrue(delta.distanceWalked() > 0);
    }

    @Test
    void testAggressivePlaythrough() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            100, 50000, 150, 5, 100
        );

        assertTrue(delta.mobsKilled() > 100);
        assertTrue(delta.deaths() > 0);
    }
}

