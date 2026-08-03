package mt.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceIntegrityTest {

    private static final Gson GSON = new Gson();

    private static final List<String> THEMES = List.of("classic", "magic", "tech", "vanilla");

    private static final List<String> THEMED_TEXTURES = List.of(
            "frame", "badge", "pages_holder", "sleeping_hud", "row",
            "mvp_row", "name_badge", "stat_badge", "achievement_badge");

    private static final List<String> SHARED_TEXTURES = List.of(
            "icons/icon_blocks", "icons/icon_distance", "icons/icon_sword",
            "icons/icon_death", "icons/icon_jump", "icons/icon_axe",
            "overlay/skull", "hud/scale", "hud/fill", "hud/well_rested",
            "hud/mvp", "summary/gear");

    private static final List<String> MOON_PHASES = List.of(
            "full_moon", "waning_gibbous", "last_quarter", "waning_crescent",
            "new_moon", "waxing_crescent", "first_quarter", "waxing_gibbous");

    private static final List<String> POSITIVE_TAGS = List.of(
            "comfort_lighting", "comfort_carpet", "comfort_furniture",
            "comfort_decoration", "comfort_structure");

    private static final List<String> NEGATIVE_TAGS = List.of(
            "comfort_negative_macabre", "comfort_negative_hostile", "comfort_negative_dark");

    private static Path assets() {
        return Paths.get("src", "main", "resources", "assets", "midnightthoughts");
    }

    private static Path tagsDir() {
        return Paths.get("src", "main", "resources", "data", "midnightthoughts", "tags", "blocks");
    }

    private static List<String> tagValues(String tag) throws IOException {
        JsonObject root = GSON.fromJson(
                Files.readString(tagsDir().resolve(tag + ".json"), StandardCharsets.UTF_8), JsonObject.class);
        List<String> values = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray("values")) {
            values.add(element.isJsonObject()
                    ? element.getAsJsonObject().get("id").getAsString()
                    : element.getAsString());
        }
        return values;
    }

    private static List<String> allTags() {
        List<String> all = new ArrayList<>(POSITIVE_TAGS);
        all.addAll(NEGATIVE_TAGS);
        return all;
    }

    @Test
    void everyThemeShipsEveryTexture() {
        for (String theme : THEMES) {
            for (String name : THEMED_TEXTURES) {
                Path file = assets().resolve("textures/gui/" + theme + "/" + name + ".png");
                assertTrue(Files.isRegularFile(file), "missing texture " + file);
                assertTrue(file.toFile().length() > 0, "empty texture " + file);
            }
        }
    }

    @Test
    void everySharedTextureExists() {
        for (String name : SHARED_TEXTURES) {
            Path file = assets().resolve("textures/gui/shared/" + name + ".png");
            assertTrue(Files.isRegularFile(file), "missing texture " + file);
        }
    }

    @Test
    void everyMoonPhaseTextureExists() {
        for (String phase : MOON_PHASES) {
            Path file = assets().resolve("textures/gui/shared/overlay/moon_phases/" + phase + ".png");
            assertTrue(Files.isRegularFile(file), "missing texture " + file);
        }
        assertEquals(8, MOON_PHASES.size(), "minecraft has exactly eight moon phases");
    }

    @Test
    void everyComfortTagExistsAndIsNotEmpty() throws IOException {
        for (String tag : allTags()) {
            Path file = tagsDir().resolve(tag + ".json");
            assertTrue(Files.isRegularFile(file), "missing tag " + file);
            assertTrue(tagValues(tag).size() > 0, "tag " + tag + " is empty");
        }
    }

    @Test
    void comfortTagEntriesAreFullyQualified() throws IOException {
        for (String tag : allTags()) {
            for (String value : tagValues(tag)) {
                assertTrue(value.startsWith("#") || value.contains(":"),
                        "entry \"" + value + "\" in " + tag + " has no namespace");
            }
        }
    }

    @Test
    void noBlockAppearsTwiceInTheSameTag() throws IOException {
        for (String tag : allTags()) {
            Set<String> seen = new HashSet<>();
            for (String value : tagValues(tag)) {
                assertTrue(seen.add(value), "duplicate " + value + " in " + tag);
            }
        }
    }

    @Test
    void noBlockIsBothCosyAndUnsettling() throws IOException {
        Map<String, List<String>> owners = new HashMap<>();
        for (String tag : allTags()) {
            for (String value : tagValues(tag)) {
                owners.computeIfAbsent(value, key -> new ArrayList<>()).add(tag);
            }
        }

        List<String> conflicts = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : owners.entrySet()) {
            if (entry.getValue().size() > 1) {
                conflicts.add(entry.getKey() + " -> " + entry.getValue());
            }
        }

        assertTrue(conflicts.isEmpty(),
                "a block scoring in two comfort categories cancels itself out: " + conflicts);
    }

    @Test
    void theModVersionMatchesTheBuildScript() throws IOException {
        String properties = Files.readString(Paths.get("gradle.properties"), StandardCharsets.UTF_8);
        String toml = Files.readString(
                Paths.get("src", "main", "resources", "META-INF", "mods.toml"), StandardCharsets.UTF_8);

        String declared = properties.lines()
                .filter(line -> line.startsWith("mod_version="))
                .map(line -> line.substring("mod_version=".length()).trim())
                .findFirst()
                .orElseThrow();
        String shortVersion = declared.split("\\+")[0];

        assertTrue(toml.contains("version = \"" + shortVersion + "\""),
                "mods.toml shows a different version than gradle.properties (" + shortVersion + ")");
    }
}
