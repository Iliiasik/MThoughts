package mt.server;

import mt.client.config.MidnightThoughtsConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AchievementCalculatorTest {

    @BeforeEach
    void setUp() {
        MidnightThoughtsConfig.getInstance();
    }

    @Test
    void testFlawlessAchievement() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            100, 1000000, 15, 0, 50
        );

        assertTrue(wouldGetAchievement("flawless", delta));
    }

    @Test
    void testFlawlessFailsWithDeaths() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            100, 1000000, 15, 1, 50
        );

        assertFalse(wouldGetAchievement("flawless", delta));
    }

    @Test
    void testPacifistAchievement() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            50, 60000000, 0, 0, 100
        );

        assertTrue(wouldGetAchievement("pacifist", delta));
    }

    @Test
    void testJuggernautAchievement() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            50, 100000, 60, 1, 50
        );

        assertTrue(wouldGetAchievement("juggernaut", delta));
    }

    @Test
    void testMarathonerAchievement() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            50, 400000000, 10, 0, 50
        );

        assertTrue(wouldGetAchievement("marathoner", delta));
    }

    @Test
    void testHyperactiveAchievement() {
        DailyPlayerStats.DailyDelta delta = new DailyPlayerStats.DailyDelta(
            50, 100000, 10, 0, 600
        );

        assertTrue(wouldGetAchievement("hyperactive", delta));
    }

    @Test
    void testDetermineMvpWithMultiplePlayers() {
        List<AchievementCalculator.PlayerSummaryData> players = new ArrayList<>();
        players.add(new AchievementCalculator.PlayerSummaryData("Player1", 100, 10000, 20, 0, 100));
        players.add(new AchievementCalculator.PlayerSummaryData("Player2", 200, 15000, 30, 1, 150));
        players.add(new AchievementCalculator.PlayerSummaryData("Player3", 50, 5000, 5, 2, 50));

        String mvp = AchievementCalculator.determineMvp(players);
        assertNotNull(mvp);
        assertEquals("Player2", mvp);
    }

    @Test
    void testDetermineMvpWithTie() {
        List<AchievementCalculator.PlayerSummaryData> players = new ArrayList<>();
        players.add(new AchievementCalculator.PlayerSummaryData("Player1", 100, 10000, 20, 0, 100));
        players.add(new AchievementCalculator.PlayerSummaryData("Player2", 100, 10000, 20, 0, 100));

        String mvp = AchievementCalculator.determineMvp(players);
        assertNull(mvp);
    }

    @Test
    void testDetermineMvpWithSinglePlayer() {
        List<AchievementCalculator.PlayerSummaryData> players = new ArrayList<>();
        players.add(new AchievementCalculator.PlayerSummaryData("Player1", 100, 10000, 20, 0, 100));

        String mvp = AchievementCalculator.determineMvp(players);
        assertNull(mvp);
    }

    @Test
    void testDetermineMvpWithLowScores() {
        List<AchievementCalculator.PlayerSummaryData> players = new ArrayList<>();
        players.add(new AchievementCalculator.PlayerSummaryData("Player1", 1, 100, 0, 0, 1));
        players.add(new AchievementCalculator.PlayerSummaryData("Player2", 2, 200, 0, 0, 2));

        String mvp = AchievementCalculator.determineMvp(players);
        assertNull(mvp);
    }

    @Test
    void testMvpScoreCalculation() {
        AchievementCalculator.PlayerSummaryData player1 =
            new AchievementCalculator.PlayerSummaryData("Player1", 100, 10000, 20, 0, 100);

        AchievementCalculator.PlayerSummaryData player2 =
            new AchievementCalculator.PlayerSummaryData("Player2", 50, 5000, 50, 5, 50);

        List<AchievementCalculator.PlayerSummaryData> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        String mvp = AchievementCalculator.determineMvp(players);
        assertEquals("Player2", mvp);
    }

    @Test
    void testMvpDisabled() {
        MidnightThoughtsConfig.getInstance().getMvp().enabled = false;

        List<AchievementCalculator.PlayerSummaryData> players = new ArrayList<>();
        players.add(new AchievementCalculator.PlayerSummaryData("Player1", 1000, 100000, 100, 0, 1000));
        players.add(new AchievementCalculator.PlayerSummaryData("Player2", 500, 50000, 50, 0, 500));

        String mvp = AchievementCalculator.determineMvp(players);
        assertNull(mvp);

        MidnightThoughtsConfig.getInstance().getMvp().enabled = true;
    }

    private boolean wouldGetAchievement(String achievementId, DailyPlayerStats.DailyDelta delta) {
        MidnightThoughtsConfig.AchievementRequirement req =
            MidnightThoughtsConfig.getInstance().getAchievements().getRequirement(achievementId);

        int blocksWalked = delta.distanceWalked() / 100;

        if (req.deaths != null && delta.deaths() != req.deaths) return false;
        if (req.deathsMax != null && delta.deaths() > req.deathsMax) return false;
        if (req.mobsMin != null && delta.mobsKilled() < req.mobsMin) return false;
        if (req.mobsMax != null && delta.mobsKilled() > req.mobsMax) return false;
        if (req.blocksMin != null && delta.blocksDestroyed() < req.blocksMin) return false;
        if (req.distanceMin != null && blocksWalked < req.distanceMin) return false;
        if (req.jumpsMin != null && delta.jumps() < req.jumpsMin) return false;

        return true;
    }
}

