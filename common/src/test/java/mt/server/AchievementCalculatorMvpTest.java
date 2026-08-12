package mt.server;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AchievementCalculatorMvpTest {

    @TempDir
    static Path gameDir;

    private static String previousUserDir;

    @BeforeAll
    static void redirectGameDirectory() {
        previousUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", gameDir.toString());
    }

    @AfterAll
    static void restoreGameDirectory() {
        System.setProperty("user.dir", previousUserDir);
    }

    private static AchievementCalculator.PlayerSummaryData player(
            String name, int blocks, int distance, int mobs, int deaths, int jumps) {
        return new AchievementCalculator.PlayerSummaryData(name, blocks, distance, mobs, deaths, jumps);
    }

    private static AchievementCalculator.PlayerSummaryData idle(String name) {
        return player(name, 0, 0, 0, 0, 0);
    }

    @Test
    void noMvpForAnEmptyLobby() {
        assertNull(AchievementCalculator.determineMvp(Collections.emptyList()));
    }

    @Test
    void noMvpForASinglePlayer() {
        assertNull(AchievementCalculator.determineMvp(List.of(player("Solo", 100, 0, 100, 0, 0))));
    }

    @Test
    void theHighestScorerWinsRegardlessOfPosition() {
        var winner = player("Winner", 10, 0, 0, 0, 0);
        var filler = player("Filler", 1, 0, 0, 0, 0);

        assertEquals("Winner", AchievementCalculator.determineMvp(List.of(winner, filler, filler)));
        assertEquals("Winner", AchievementCalculator.determineMvp(List.of(filler, winner, filler)));
        assertEquals("Winner", AchievementCalculator.determineMvp(List.of(filler, filler, winner)));
    }

    @Test
    void aTieProducesNoMvp() {
        var first = player("First", 10, 0, 0, 0, 0);
        var second = player("Second", 10, 0, 0, 0, 0);

        assertNull(AchievementCalculator.determineMvp(List.of(first, second)));
    }

    @Test
    void aTieAmongLeadersProducesNoMvpEvenWithATrailingPlayer() {
        var first = player("First", 10, 0, 0, 0, 0);
        var second = player("Second", 10, 0, 0, 0, 0);
        var trailing = player("Trailing", 1, 0, 0, 0, 0);

        assertNull(AchievementCalculator.determineMvp(List.of(first, second, trailing)));
        assertNull(AchievementCalculator.determineMvp(List.of(trailing, first, second)));
    }

    @Test
    void scoresBelowTheThresholdProduceNoMvp() {
        var barelyActive = player("Barely", 1, 0, 0, 0, 0);
        var idle = idle("Idle");

        assertNull(AchievementCalculator.determineMvp(List.of(barelyActive, idle)));
    }

    @Test
    void everyoneIdleProducesNoMvp() {
        assertNull(AchievementCalculator.determineMvp(List.of(idle("A"), idle("B"), idle("C"))));
    }

    @Test
    void aLeadingZeroScoreDoesNotBlockALaterWinner() {
        var idle = idle("Idle");
        var active = player("Active", 10, 0, 0, 0, 0);

        assertEquals("Active", AchievementCalculator.determineMvp(List.of(idle, active)));
    }

    @Test
    void deathsCanCancelOutAnOtherwiseWinningScore() {
        var reckless = player("Reckless", 0, 0, 1, 1, 0);
        var steady = player("Steady", 5, 0, 0, 0, 0);

        assertEquals("Steady", AchievementCalculator.determineMvp(List.of(reckless, steady)));
    }

    @Test
    void scoresNeverGoNegative() {
        var doomed = player("Doomed", 0, 0, 0, 100, 0);
        var winner = player("Winner", 4, 0, 0, 0, 0);

        assertEquals("Winner", AchievementCalculator.determineMvp(List.of(doomed, winner)));
    }

    @Test
    void distanceContributesToTheScore() {
        var walker = player("Walker", 0, 1_000_000, 0, 0, 0);
        var idle = idle("Idle");

        assertEquals("Walker", AchievementCalculator.determineMvp(List.of(walker, idle)));
    }

    @Test
    void shortDistancesDoNotReachTheThreshold() {
        var stroller = player("Stroller", 0, 9_999, 0, 0, 0);
        var idle = idle("Idle");

        assertNull(AchievementCalculator.determineMvp(List.of(stroller, idle)));
    }

    @Test
    void mobsAndJumpsContributeToTheScore() {
        var hunter = player("Hunter", 0, 0, 1, 0, 0);
        var jumper = player("Jumper", 0, 0, 0, 0, 200);
        var idle = idle("Idle");

        assertEquals("Jumper", AchievementCalculator.determineMvp(List.of(jumper, hunter, idle)));
        assertEquals("Hunter", AchievementCalculator.determineMvp(List.of(hunter, idle)));
    }

    @Test
    void theWinnerIsAlwaysOneOfThePlayers() {
        List<AchievementCalculator.PlayerSummaryData> players = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            players.add(player("P" + i, i, i * 1000, i % 5, i % 3, i * 7));
        }

        String mvp = AchievementCalculator.determineMvp(players);

        assertNotNull(mvp);
        assertTrue(players.stream().anyMatch(p -> p.playerName().equals(mvp)),
                "mvp " + mvp + " is not in the lobby");
    }

    @Test
    void resultDoesNotDependOnInputOrderWhenThereIsAClearWinner() {
        List<AchievementCalculator.PlayerSummaryData> players = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            players.add(player("P" + i, i, 0, 0, 0, 0));
        }
        String expected = AchievementCalculator.determineMvp(players);
        assertEquals("P19", expected);

        for (int shuffle = 0; shuffle < 200; shuffle++) {
            Collections.shuffle(players);
            assertEquals(expected, AchievementCalculator.determineMvp(players),
                    "mvp changed with input order");
        }
    }

    @Test
    void extremeStatsNeverThrow() {
        List<AchievementCalculator.PlayerSummaryData> players = new ArrayList<>();
        players.add(player("Normal", 5, 0, 0, 0, 0));
        players.add(player("Extreme", Integer.MAX_VALUE, Integer.MAX_VALUE,
                Integer.MAX_VALUE, 0, Integer.MAX_VALUE));

        String mvp = AchievementCalculator.determineMvp(players);

        assertTrue(mvp == null || mvp.equals("Extreme") || mvp.equals("Normal"),
                "unexpected mvp: " + mvp);
    }

    @Test
    void determinationUnderLoadStaysConsistent() {
        List<AchievementCalculator.PlayerSummaryData> players =
                List.of(player("A", 20, 0, 0, 0, 0), player("B", 5, 0, 0, 0, 0));

        for (int i = 0; i < 200_000; i++) {
            assertEquals("A", AchievementCalculator.determineMvp(players));
        }
    }
}
