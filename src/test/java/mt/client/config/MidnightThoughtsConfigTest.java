package mt.client.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MidnightThoughtsConfigTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        System.setProperty("user.dir", tempDir.toString());
    }

    @Test
    void testDefaultConfigCreation() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();

        assertNotNull(config);
        assertNotNull(config.getSleepOverlay());
        assertNotNull(config.getWellRested());
        assertNotNull(config.getAchievements());
        assertNotNull(config.getMvp());
        assertNotNull(config.getComfort());
    }

    @Test
    void testSleepOverlayDefaults() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        MidnightThoughtsConfig.SleepOverlaySettings overlay = config.getSleepOverlay();

        assertEquals(2500, overlay.minSlideDisplayTimeMs);
        assertEquals(4000, overlay.maxSlideDisplayTimeMs);
        assertEquals(300, overlay.fadeInDurationMs);
        assertEquals(300, overlay.fadeOutDurationMs);
        assertEquals(0.4f, overlay.overlayOpacity);
        assertEquals(1.0f, overlay.textOpacity);
        assertEquals(0.6f, overlay.imageOpacity);
        assertEquals(0.05f, overlay.specialSlideChance);
        assertTrue(overlay.enableOverlay);
        assertTrue(overlay.enableImage);
    }

    @Test
    void testWellRestedDefaults() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        MidnightThoughtsConfig.WellRestedSettings wellRested = config.getWellRested();

        assertNotNull(wellRested.levels);
        assertEquals(5, wellRested.levels.size());

        MidnightThoughtsConfig.WellRestedLevel level1 = wellRested.getLevel(1);
        assertEquals(3, level1.durationMinutes);
        assertEquals(2.0, level1.healthBonus);
        assertEquals(0.25, level1.luckBonus);
        assertEquals(0.005f, level1.exhaustionReduction);
        assertEquals(2.0f, level1.instantHeal);

        MidnightThoughtsConfig.WellRestedLevel level5 = wellRested.getLevel(5);
        assertEquals(15, level5.durationMinutes);
        assertEquals(4.0f, level5.instantHeal);
    }

    @Test
    void testAchievementsDefaults() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        MidnightThoughtsConfig.AchievementsSettings achievements = config.getAchievements();

        assertNotNull(achievements.requirements);
        assertEquals(10, achievements.requirements.size());

        MidnightThoughtsConfig.AchievementRequirement flawless = achievements.getRequirement("flawless");
        assertNotNull(flawless);
        assertEquals(0, flawless.deaths);
        assertEquals(10, flawless.mobsMin);
        assertEquals(50, flawless.blocksMin);
    }

    @Test
    void testMvpDefaults() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        MidnightThoughtsConfig.MvpSettings mvp = config.getMvp();

        assertTrue(mvp.enabled);
        assertEquals(10, mvp.minScoreRequired);
        assertEquals(2, mvp.pointsPerDistance100);
        assertEquals(3, mvp.pointsPerBlock);
        assertEquals(15, mvp.pointsPerMob);
        assertEquals(1, mvp.pointsPerJump10);
        assertEquals(30, mvp.penaltyPerDeath);
    }

    @Test
    void testComfortDefaults() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        MidnightThoughtsConfig.ComfortSettings comfort = config.getComfort();

        assertTrue(comfort.enabled);
        assertEquals(5, comfort.scanRadius);
    }

    @Test
    void testConfigSaveAndLoad() throws IOException {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();
        config.save();

        Path configFile = tempDir.resolve("config").resolve("midnightthoughts.json");
        assertTrue(Files.exists(configFile));

        String content = Files.readString(configFile);
        assertTrue(content.contains("sleepOverlay"));
        assertTrue(content.contains("wellRested"));
        assertTrue(content.contains("achievements"));
        assertTrue(content.contains("mvp"));
        assertTrue(content.contains("comfort"));
    }

    @Test
    void testRandomSlideDisplayTime() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();

        for (int i = 0; i < 100; i++) {
            int time = config.getRandomSlideDisplayTime();
            assertTrue(time >= 2500 && time <= 4000);
        }
    }

    @Test
    void testConfigGetters() {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.getInstance();

        assertEquals(300, config.getFadeInDurationMs());
        assertEquals(300, config.getFadeOutDurationMs());
        assertEquals(0.4f, config.getOverlayOpacity());
        assertEquals(1.0f, config.getTextOpacity());
        assertEquals(0.05f, config.getSpecialSlideChance());
        assertTrue(config.isEnableOverlay());
        assertTrue(config.isEnableImage());
    }
}

