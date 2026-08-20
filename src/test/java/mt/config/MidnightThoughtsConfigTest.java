package mt.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MidnightThoughtsConfigTest {

    private static final Gson GSON = new Gson();

    @TempDir
    Path gameDir;

    private String previousUserDir;

    @BeforeEach
    void redirectGameDirectory() {
        previousUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", gameDir.toString());
    }

    @AfterEach
    void restoreGameDirectory() {
        System.setProperty("user.dir", previousUserDir);
    }

    private Path configFile() {
        return gameDir.resolve("config").resolve("midnightthoughts").resolve("midnightthoughts.json");
    }

    private Path brokenFile() {
        return gameDir.resolve("config").resolve("midnightthoughts").resolve("midnightthoughts.json.broken");
    }

    private void writeConfig(String json) throws IOException {
        Path file = configFile();
        Files.createDirectories(file.getParent());
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }

    private JsonObject readConfig() throws IOException {
        return GSON.fromJson(Files.readString(configFile(), StandardCharsets.UTF_8), JsonObject.class);
    }

    @Test
    void missingFileProducesDefaultsAndWritesThemOut() throws IOException {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertNotNull(config);
        assertTrue(Files.isRegularFile(configFile()), "config file was not created");
        assertEquals(6000, config.getSleepOverlay().minSlideDisplayTimeMs);
        assertEquals(8000, config.getSleepOverlay().maxSlideDisplayTimeMs);
        assertEquals("classic", config.getUi().theme);
        assertFalse(config.getSleepOverlay().useFactsApi);
        assertEquals(5, config.getWellRested().levels.size());
    }

    @Test
    void malformedJsonIsBackedUpAndRegenerated() throws IOException {
        writeConfig("{ this is not json ");

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertNotNull(config);
        assertTrue(Files.isRegularFile(brokenFile()), "broken config was not backed up");
        assertEquals(6000, config.getSleepOverlay().minSlideDisplayTimeMs);
        assertTrue(Files.isRegularFile(configFile()), "config was not regenerated");
    }

    @Test
    void wrongValueTypeIsBackedUpAndRegenerated() throws IOException {
        writeConfig("{\"sleepOverlay\":{\"minSlideDisplayTimeMs\":\"not a number\"}}");

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertNotNull(config);
        assertTrue(Files.isRegularFile(brokenFile()), "broken config was not backed up");
        assertEquals(6000, config.getSleepOverlay().minSlideDisplayTimeMs);
    }

    @Test
    void emptyFileFallsBackToDefaults() throws IOException {
        writeConfig("");

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertNotNull(config);
        assertEquals(6000, config.getSleepOverlay().minSlideDisplayTimeMs);
    }

    @Test
    void jsonNullFallsBackToDefaults() throws IOException {
        writeConfig("null");

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertNotNull(config);
        assertEquals(6000, config.getSleepOverlay().minSlideDisplayTimeMs);
    }

    @Test
    void hugeIntegersAreClamped() throws IOException {
        writeConfig("""
                {
                  "sleepOverlay": {
                    "minSlideDisplayTimeMs": 2147483647,
                    "maxSlideDisplayTimeMs": 2147483647,
                    "fadeInDurationMs": 999999999,
                    "fadeOutDurationMs": -999999999
                  },
                  "comfort": { "scanRadius": 1000 },
                  "mvp": { "mvpWellRestedDurationMinutes": 100000 }
                }
                """);

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertEquals(120000, config.getSleepOverlay().minSlideDisplayTimeMs);
        assertEquals(120000, config.getSleepOverlay().maxSlideDisplayTimeMs);
        assertEquals(10000, config.getSleepOverlay().fadeInDurationMs);
        assertEquals(0, config.getSleepOverlay().fadeOutDurationMs);
        assertEquals(8, config.getComfort().scanRadius);
        assertEquals(1440, config.getMvp().mvpWellRestedDurationMinutes);
    }

    @Test
    void negativeAndOversizedOpacitiesAreClamped() throws IOException {
        writeConfig("""
                {
                  "sleepOverlay": {
                    "overlayOpacity": -3.5,
                    "textOpacity": 42.0,
                    "imageOpacity": 0.5,
                    "specialSlideChance": 7.0
                  }
                }
                """);

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertEquals(0.0f, config.getSleepOverlay().overlayOpacity);
        assertEquals(1.0f, config.getSleepOverlay().textOpacity);
        assertEquals(0.5f, config.getSleepOverlay().imageOpacity);
        assertEquals(1.0f, config.getSleepOverlay().specialSlideChance);
    }

    @Test
    void nonFiniteFloatsNeverSurviveValidation() throws IOException {
        writeConfig("""
                {
                  "sleepOverlay": {
                    "overlayOpacity": 1e40,
                    "textOpacity": -1e40,
                    "imageOpacity": 1e40
                  }
                }
                """);

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();
        MidnightThoughtsConfig.SleepOverlaySettings overlay = config.getSleepOverlay();

        for (float value : new float[]{overlay.overlayOpacity, overlay.textOpacity, overlay.imageOpacity}) {
            assertTrue(Float.isFinite(value), "opacity is not finite");
            assertTrue(value >= 0.0f && value <= 1.0f, "opacity out of range: " + value);
        }
    }

    @Test
    void maxDisplayTimeIsRaisedAboveMin() throws IOException {
        writeConfig("{\"sleepOverlay\":{\"minSlideDisplayTimeMs\":9000,\"maxSlideDisplayTimeMs\":1000}}");

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertEquals(9000, config.getSleepOverlay().minSlideDisplayTimeMs);
        assertEquals(9000, config.getSleepOverlay().maxSlideDisplayTimeMs);
        assertTrue(config.getSleepOverlay().maxSlideDisplayTimeMs >= config.getSleepOverlay().minSlideDisplayTimeMs);
    }

    @Test
    void randomSlideTimeStaysWithinTheConfiguredRange() throws IOException {
        writeConfig("{\"sleepOverlay\":{\"minSlideDisplayTimeMs\":5000,\"maxSlideDisplayTimeMs\":5200}}");
        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        for (int i = 0; i < 100_000; i++) {
            int value = config.getRandomSlideDisplayTime();
            assertTrue(value >= 5000 && value < 5200, "slide time out of range: " + value);
        }
    }

    @Test
    void equalMinAndMaxDoNotThrow() throws IOException {
        writeConfig("{\"sleepOverlay\":{\"minSlideDisplayTimeMs\":7000,\"maxSlideDisplayTimeMs\":7000}}");
        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        for (int i = 0; i < 1000; i++) {
            assertEquals(7000, config.getRandomSlideDisplayTime());
        }
    }

    @Test
    void unknownThemeIsResetToClassic() throws IOException {
        writeConfig("{\"ui\":{\"theme\":\"holographic\"}}");

        assertEquals("classic", MidnightThoughtsConfig.load().getUi().theme);
    }

    @Test
    void nullThemeIsResetToClassic() throws IOException {
        writeConfig("{\"ui\":{\"theme\":null}}");

        assertEquals("classic", MidnightThoughtsConfig.load().getUi().theme);
    }

    @Test
    void onlyTheFourShippedThemesAreValid() {
        for (String theme : new String[]{"classic", "magic", "tech", "vanilla"}) {
            assertTrue(MidnightThoughtsConfigValidation.isValidTheme(theme), theme + " must be valid");
        }
        for (String theme : new String[]{"banana", "", "CLASSIC", "classic ", "../evil"}) {
            assertFalse(MidnightThoughtsConfigValidation.isValidTheme(theme),
                    "\"" + theme + "\" must not reach a texture path");
        }
        assertFalse(MidnightThoughtsConfigValidation.isValidTheme(null));
    }

    @Test
    void unknownHudPositionIsResetToLeft() throws IOException {
        writeConfig("{\"ui\":{\"wellRestedHudPosition\":\"floating\"}}");

        assertEquals("left", MidnightThoughtsConfig.load().getUi().wellRestedHudPosition);
    }

    @Test
    void allHudPositionsArePreserved() throws IOException {
        for (String position : new String[]{"left", "bar", "right"}) {
            writeConfig("{\"ui\":{\"wellRestedHudPosition\":\"" + position + "\"}}");
            assertEquals(position, MidnightThoughtsConfig.load().getUi().wellRestedHudPosition);
        }
    }

    @Test
    void hastePhasesAreClamped() throws IOException {
        writeConfig("""
                {
                  "wellRested": {
                    "levels": {
                      "level1": { "hastePhase1": 900.0, "hastePhase2": -900.0, "hastePhase3": 0.25 }
                    }
                  }
                }
                """);

        MidnightThoughtsConfig.WellRestedLevel level = MidnightThoughtsConfig.load().getWellRested().getLevel(1);

        assertEquals(5.0f, level.hastePhase1);
        assertEquals(-5.0f, level.hastePhase2);
        assertEquals(0.25f, level.hastePhase3);
    }

    @Test
    void everyValidThemeIsPreserved() throws IOException {
        for (String theme : new String[]{"classic", "magic", "tech", "vanilla"}) {
            writeConfig("{\"ui\":{\"theme\":\"" + theme + "\"}}");
            assertEquals(theme, MidnightThoughtsConfig.load().getUi().theme);
        }
    }

    @Test
    void missingSectionsAreRestoredWithDefaults() throws IOException {
        writeConfig("{\"sleepOverlay\":{\"minSlideDisplayTimeMs\":4000}}");

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertNotNull(config.getWellRested());
        assertNotNull(config.getMvp());
        assertNotNull(config.getComfort());
        assertNotNull(config.getComfort().weights);
        assertNotNull(config.getServer());
        assertNotNull(config.getUi());
        assertEquals(4000, config.getSleepOverlay().minSlideDisplayTimeMs);
        assertEquals(10, config.getMvp().minScoreRequired);
    }

    @Test
    void explicitNullSectionsAreRestoredWithDefaults() throws IOException {
        writeConfig("{\"sleepOverlay\":null,\"wellRested\":null,\"mvp\":null,\"comfort\":null,\"server\":null,\"ui\":null}");

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertNotNull(config.getSleepOverlay());
        assertNotNull(config.getWellRested());
        assertNotNull(config.getMvp());
        assertNotNull(config.getComfort());
        assertNotNull(config.getComfort().weights);
        assertNotNull(config.getServer());
        assertNotNull(config.getUi());
        assertEquals("classic", config.getUi().theme);
    }

    @Test
    void obsoleteKeysAreDroppedOnMigration() throws IOException {
        writeConfig("""
                {
                  "sleepOverlay": { "minSlideDisplayTimeMs": 4500, "legacySlideMode": "spooky" },
                  "removedSection": { "whatever": 1 },
                  "ui": { "theme": "tech", "oldThemeIndex": 3 }
                }
                """);

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();
        JsonObject onDisk = readConfig();

        assertEquals(4500, config.getSleepOverlay().minSlideDisplayTimeMs);
        assertEquals("tech", config.getUi().theme);
        assertFalse(onDisk.has("removedSection"), "obsolete section survived migration");
        assertFalse(onDisk.getAsJsonObject("sleepOverlay").has("legacySlideMode"),
                "obsolete key survived migration");
        assertFalse(onDisk.getAsJsonObject("ui").has("oldThemeIndex"),
                "obsolete key survived migration");
    }

    @Test
    void newKeysAppearAfterMigratingAnOldConfig() throws IOException {
        writeConfig("""
                {
                  "sleepOverlay": { "minSlideDisplayTimeMs": 4500, "maxSlideDisplayTimeMs": 7500 },
                  "ui": { "theme": "magic" }
                }
                """);

        MidnightThoughtsConfig.load();
        JsonObject onDisk = readConfig();
        JsonObject overlay = onDisk.getAsJsonObject("sleepOverlay");

        assertTrue(overlay.has("enableStarDust"), "new key missing after migration");
        assertTrue(overlay.has("showSlideProgress"), "new key missing after migration");
        assertTrue(onDisk.getAsJsonObject("ui").has("hideWellRestedHud"));
        assertTrue(onDisk.getAsJsonObject("ui").has("hideSleepingPlayersHud"));
        assertTrue(onDisk.getAsJsonObject("ui").has("hideThemeSwitchButton"));
        assertEquals(4500, overlay.get("minSlideDisplayTimeMs").getAsInt());
        assertEquals(7500, overlay.get("maxSlideDisplayTimeMs").getAsInt());
        assertEquals("magic", onDisk.getAsJsonObject("ui").get("theme").getAsString());
    }

    @Test
    void missingWellRestedLevelsAreRestored() throws IOException {
        writeConfig("{\"wellRested\":{\"enabled\":true,\"levels\":{\"level1\":{\"durationMinutes\":4}}}}");

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertEquals(5, config.getWellRested().levels.size());
        assertEquals(4, config.getWellRested().getLevel(1).durationMinutes);
        for (int level = 1; level <= 5; level++) {
            assertNotNull(config.getWellRested().getLevel(level), "level" + level + " missing");
        }
    }

    @Test
    void nullWellRestedLevelMapIsRestored() throws IOException {
        writeConfig("{\"wellRested\":{\"enabled\":true,\"levels\":null}}");

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertEquals(5, config.getWellRested().levels.size());
    }

    @Test
    void wellRestedLevelValuesAreClamped() throws IOException {
        writeConfig("""
                {
                  "wellRested": {
                    "levels": {
                      "level1": {
                        "durationMinutes": 0,
                        "speedPhase1": 900.0,
                        "strengthPhase1": -900.0,
                        "healthBonus": -5.0,
                        "regenBonus": 55.0
                      }
                    }
                  }
                }
                """);

        MidnightThoughtsConfig.WellRestedLevel level = MidnightThoughtsConfig.load().getWellRested().getLevel(1);

        assertEquals(1, level.durationMinutes);
        assertEquals(5.0f, level.speedPhase1);
        assertEquals(-5.0f, level.strengthPhase1);
        assertEquals(0.0f, level.healthBonus);
        assertEquals(1.0f, level.regenBonus);
    }

    @Test
    void unknownLevelKeyFallsBackToLevelOne() throws IOException {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertEquals(config.getWellRested().getLevel(1).durationMinutes,
                config.getWellRested().getLevel(99).durationMinutes);
        assertEquals(config.getWellRested().getLevel(1).durationMinutes,
                config.getWellRested().getLevel(0).durationMinutes);
        assertEquals(config.getWellRested().getLevel(1).durationMinutes,
                config.getWellRested().getLevel(-1).durationMinutes);
    }

    @Test
    void comfortWeightsAreClamped() throws IOException {
        writeConfig("""
                {
                  "comfort": {
                    "nightmareThreshold": -5000,
                    "sleepBlockThreshold": 5000,
                    "weights": { "lighting": 99999, "macabre": -99999 }
                  }
                }
                """);

        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

        assertEquals(-100, config.getComfort().nightmareThreshold);
        assertEquals(100, config.getComfort().sleepBlockThreshold);
        assertEquals(100, config.getComfort().weights.lighting);
        assertEquals(-100, config.getComfort().weights.macabre);
    }

    @Test
    void validationIsIdempotent() throws IOException {
        writeConfig("{\"sleepOverlay\":{\"overlayOpacity\":5.0,\"minSlideDisplayTimeMs\":-4}}");

        MidnightThoughtsConfig.load();
        String firstPass = Files.readString(configFile(), StandardCharsets.UTF_8);
        MidnightThoughtsConfig.load();
        String secondPass = Files.readString(configFile(), StandardCharsets.UTF_8);

        assertEquals(firstPass, secondPass, "validation is not stable across loads");
    }

    @Test
    void repeatedLoadAndSaveCyclesStayStable() throws IOException {
        String defaults = null;
        for (int i = 0; i < 500; i++) {
            MidnightThoughtsConfig config = MidnightThoughtsConfig.load();
            config.save();
            String current = Files.readString(configFile(), StandardCharsets.UTF_8);
            if (defaults == null) {
                defaults = current;
            } else {
                assertEquals(defaults, current, "config drifted on cycle " + i);
            }
        }
        assertFalse(Files.isRegularFile(brokenFile()), "a clean config was wrongly flagged as broken");
    }

    @Test
    void everyGarbageConfigStillYieldsAUsableConfig() throws IOException {
        String[] garbage = {
                "[]",
                "\"a string\"",
                "12345",
                "true",
                "{",
                "{}",
                "{\"sleepOverlay\":[]}",
                "{\"wellRested\":{\"levels\":[]}}",
                "{\"comfort\":{\"weights\":\"nope\"}}",
                "{\"ui\":{\"theme\":123}}"
        };

        for (String content : garbage) {
            Files.deleteIfExists(configFile());
            Files.deleteIfExists(brokenFile());
            writeConfig(content);

            MidnightThoughtsConfig config = MidnightThoughtsConfig.load();

            assertNotNull(config, "load returned null for input: " + content);
            assertNotNull(config.getSleepOverlay(), "null overlay for input: " + content);
            assertNotNull(config.getWellRested(), "null wellRested for input: " + content);
            assertNotNull(config.getComfort().weights, "null weights for input: " + content);
            assertNotNull(config.getUi().theme, "null theme for input: " + content);
            assertTrue(config.getSleepOverlay().maxSlideDisplayTimeMs
                    >= config.getSleepOverlay().minSlideDisplayTimeMs, "bad range for input: " + content);
        }
    }

    @Test
    void savingLeavesNoTemporaryFileBehind() throws IOException {
        MidnightThoughtsConfig config = MidnightThoughtsConfig.load();
        for (int i = 0; i < 50; i++) {
            config.save();
        }

        try (var entries = Files.list(configFile().getParent())) {
            assertTrue(entries.noneMatch(path -> path.getFileName().toString().endsWith(".tmp")),
                    "an atomic write left a stray temporary file");
        }
    }

    @Test
    void savedConfigIsReadableAsJson() throws IOException {
        MidnightThoughtsConfig.load();

        JsonObject onDisk = readConfig();

        assertTrue(onDisk.has("sleepOverlay"));
        assertTrue(onDisk.has("wellRested"));
        assertTrue(onDisk.has("mvp"));
        assertTrue(onDisk.has("comfort"));
        assertTrue(onDisk.has("server"));
        assertTrue(onDisk.has("ui"));
        assertEquals(6, onDisk.keySet().size(), "unexpected top level keys: " + onDisk.keySet());
    }
}
