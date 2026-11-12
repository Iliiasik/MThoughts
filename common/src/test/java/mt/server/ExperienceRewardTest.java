package mt.server;

import mt.config.MidnightThoughtsConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExperienceRewardTest {

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
        MidnightThoughtsConfig.recreate();
        System.setProperty("user.dir", previousUserDir);
    }

    @BeforeEach
    void defaultConfig() {
        MidnightThoughtsConfig.recreate();
    }

    private static AchievementCalculator.PlayerSummaryData player(int blocks, int distance, int mobs, int deaths, int jumps) {
        return new AchievementCalculator.PlayerSummaryData("Player", blocks, distance, mobs, deaths, jumps);
    }

    private static int experience(int blocks, int distance, int mobs, int deaths, int jumps) {
        return AchievementCalculator.experienceFor(player(blocks, distance, mobs, deaths, jumps));
    }

    @Test
    void theDefaultRateIsThreeHundredths() {
        assertEquals(0.03f, MidnightThoughtsConfig.getInstance().getExperience().experiencePerPoint);
    }

    @Test
    void fiveHundredMinedBlocksGiveFortyFiveExperience() {
        assertEquals(1500, AchievementCalculator.score(player(500, 0, 0, 0, 0)));
        assertEquals(45, experience(500, 0, 0, 0, 0));
    }

    @Test
    void twoThousandBlocksWalkedGiveOneExperience() {
        assertEquals(40, AchievementCalculator.score(player(0, 200_000, 0, 0, 0)));
        assertEquals(1, experience(0, 200_000, 0, 0, 0));
    }

    @Test
    void anIdleDayGivesNothing() {
        assertEquals(0, experience(0, 0, 0, 0, 0));
    }

    @Test
    void everyActionAddsToTheSameScore() {
        assertEquals(350, AchievementCalculator.score(player(100, 50_000, 2, 0, 100)));
        assertEquals(10, experience(100, 50_000, 2, 0, 100));
    }

    @Test
    void fractionsAreRoundedDown() {
        assertEquals(0, experience(11, 0, 0, 0, 0));
        assertEquals(1, experience(12, 0, 0, 0, 0));
        assertEquals(1, experience(22, 0, 0, 0, 0));
        assertEquals(2, experience(23, 0, 0, 0, 0));
    }

    @Test
    void deathsNeverMakeExperienceNegative() {
        assertEquals(0, experience(0, 0, 1, 5, 0));
    }

    @Test
    void theSwitchTurnsExperienceOff() {
        MidnightThoughtsConfig.getInstance().getExperience().enabled = false;

        assertEquals(0, experience(500, 0, 0, 0, 0));
    }

    @Test
    void aZeroRateGivesNothing() {
        MidnightThoughtsConfig.getInstance().getExperience().experiencePerPoint = 0f;

        assertEquals(0, experience(5000, 0, 0, 0, 0));
    }

    @Test
    void theConfiguredRateIsUsed() {
        MidnightThoughtsConfig.getInstance().getExperience().experiencePerPoint = 0.5f;

        assertEquals(15, experience(10, 0, 0, 0, 0));
    }

    @Test
    void experienceDoesNotDependOnTheMvpSwitch() {
        MidnightThoughtsConfig.getInstance().getMvp().enabled = false;

        assertEquals(45, experience(500, 0, 0, 0, 0));
    }

    @Test
    void changedPointValuesChangeTheExperience() {
        MidnightThoughtsConfig.getInstance().getMvp().pointsPerBlock = 1;

        assertEquals(15, experience(500, 0, 0, 0, 0));
    }

    @Test
    void theLimitCapsOneSleep() {
        MidnightThoughtsConfig.getInstance().getExperience().maxExperiencePerSleep = 30;

        assertEquals(30, experience(5000, 0, 0, 0, 0));
        assertEquals(9, experience(100, 0, 0, 0, 0));
        assertEquals(30, experience(334, 0, 0, 0, 0));
    }

    @Test
    void aZeroLimitMeansNoLimit() {
        assertEquals(0, MidnightThoughtsConfig.getInstance().getExperience().maxExperiencePerSleep);
        assertEquals(450, experience(5000, 0, 0, 0, 0));
    }

    @Test
    void aHugeDayIsCapped() {
        MidnightThoughtsConfig.getInstance().getExperience().experiencePerPoint = 10f;

        assertEquals(1_000_000, experience(Integer.MAX_VALUE / 8, 0, 0, 0, 0));
    }
}
