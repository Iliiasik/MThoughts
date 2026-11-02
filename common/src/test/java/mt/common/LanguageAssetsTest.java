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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageAssetsTest {

    private static final Gson GSON = new Gson();
    private static final String UNIT_PREFIX = "midnightthoughts.summary.unit.";
    private static final List<String> UNITS = List.of("blocks", "mobs", "times", "damage");
    private static final List<String> CATEGORIES =
            List.of("facts", "lore", "nightmares", "special", "surreal");

    private static Path projectDir() {
        String configured = System.getProperty("midnightthoughts.projectDir");
        return configured != null ? Paths.get(configured) : Paths.get("");
    }

    private static Path assetsRoot() {
        return projectDir().resolve(Paths.get("src", "main", "resources", "assets", "midnightthoughts"));
    }

    private static Path dreamsFile(String language, String category) {
        return assetsRoot().resolve("dreams").resolve(language).resolve(category + ".json");
    }

    private static Path langFile(String language) {
        return assetsRoot().resolve("lang").resolve(language + ".json");
    }

    private static JsonObject lang(String language) throws IOException {
        return GSON.fromJson(Files.readString(langFile(language), StandardCharsets.UTF_8), JsonObject.class);
    }

    private static boolean isPluralForm(String key) {
        return key.startsWith(UNIT_PREFIX);
    }

    private static Set<String> pluralFormsFor(String language) {
        String family = language.substring(0, 2);
        return switch (family) {
            case "ru", "uk", "be", "pl" -> Set.of("one", "few", "many");
            case "ja", "ko", "zh", "tr" -> Set.of("other");
            default -> Set.of("one", "other");
        };
    }

    private static List<JsonObject> entries(Path file) throws IOException {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        JsonObject root = GSON.fromJson(json, JsonObject.class);
        assertTrue(root != null && root.has("entries"),
                file + " has no \"entries\" array");
        List<JsonObject> result = new ArrayList<>();
        for (JsonElement element : root.getAsJsonArray("entries")) {
            result.add(element.getAsJsonObject());
        }
        return result;
    }

    @Test
    void everyLanguageHasEveryDreamFile() {
        for (String language : SupportedLanguages.codes()) {
            for (String category : CATEGORIES) {
                Path file = dreamsFile(language, category);
                assertTrue(Files.isRegularFile(file), "missing " + file);
                assertTrue(file.toFile().length() > 0, "empty file " + file);
            }
        }
    }

    @Test
    void everyLanguageHasEveryLangFile() {
        for (String language : SupportedLanguages.codes()) {
            Path file = langFile(language);
            assertTrue(Files.isRegularFile(file), "missing " + file);
            assertTrue(file.toFile().length() > 0, "empty file " + file);
        }
    }

    @Test
    void dreamEntryCountsMatchEnglish() throws IOException {
        for (String category : CATEGORIES) {
            int expected = entries(dreamsFile(SupportedLanguages.DEFAULT, category)).size();
            assertTrue(expected > 0, "english " + category + " is empty");
            for (String language : SupportedLanguages.codes()) {
                assertEquals(expected, entries(dreamsFile(language, category)).size(),
                        "entry count drift in dreams/" + language + "/" + category + ".json");
            }
        }
    }

    @Test
    void dreamRaritiesAreIndexAlignedWithEnglish() throws IOException {
        for (String category : CATEGORIES) {
            List<JsonObject> reference = entries(dreamsFile(SupportedLanguages.DEFAULT, category));
            for (String language : SupportedLanguages.codes()) {
                List<JsonObject> actual = entries(dreamsFile(language, category));
                for (int i = 0; i < reference.size(); i++) {
                    assertEquals(
                            reference.get(i).get("rarity").getAsDouble(),
                            actual.get(i).get("rarity").getAsDouble(),
                            1e-9,
                            "rarity mismatch at index " + i + " in dreams/" + language + "/" + category + ".json"
                    );
                }
            }
        }
    }

    @Test
    void noDreamEntryIsBlank() throws IOException {
        for (String language : SupportedLanguages.codes()) {
            for (String category : CATEGORIES) {
                List<JsonObject> all = entries(dreamsFile(language, category));
                for (int i = 0; i < all.size(); i++) {
                    JsonObject entry = all.get(i);
                    assertTrue(entry.has("text"),
                            "missing text at index " + i + " in dreams/" + language + "/" + category + ".json");
                    assertFalse(entry.get("text").getAsString().isBlank(),
                            "blank text at index " + i + " in dreams/" + language + "/" + category + ".json");
                }
            }
        }
    }

    @Test
    void noDuplicateDreamTextWithinAFile() throws IOException {
        for (String language : SupportedLanguages.codes()) {
            for (String category : CATEGORIES) {
                List<JsonObject> all = entries(dreamsFile(language, category));
                Set<String> seen = new TreeSet<>();
                for (JsonObject entry : all) {
                    String text = entry.get("text").getAsString().trim();
                    assertTrue(seen.add(text),
                            "duplicate entry \"" + text + "\" in dreams/" + language + "/" + category + ".json");
                }
            }
        }
    }

    @Test
    void translationKeysMatchEnglish() throws IOException {
        Set<String> expected = new TreeSet<>(lang(SupportedLanguages.DEFAULT).keySet());
        expected.removeIf(LanguageAssetsTest::isPluralForm);
        assertFalse(expected.isEmpty(), "english lang file is empty");

        for (String language : SupportedLanguages.codes()) {
            JsonObject actual = lang(language);
            Set<String> keys = new TreeSet<>(actual.keySet());
            keys.removeIf(LanguageAssetsTest::isPluralForm);
            assertEquals(expected, keys, "translation key drift in lang/" + language + ".json");
            for (Map.Entry<String, JsonElement> entry : actual.entrySet()) {
                assertFalse(entry.getValue().getAsString().isBlank(),
                        "blank translation for " + entry.getKey() + " in lang/" + language + ".json");
            }
        }
    }

    @Test
    void pluralFormsFollowEachLanguageRule() throws IOException {
        for (String language : SupportedLanguages.codes()) {
            JsonObject actual = lang(language);
            for (String unit : UNITS) {
                Set<String> forms = new TreeSet<>();
                for (String key : actual.keySet()) {
                    if (key.startsWith(UNIT_PREFIX + unit + ".")) forms.add(key.substring((UNIT_PREFIX + unit + ".").length()));
                }
                assertEquals(new TreeSet<>(pluralFormsFor(language)), forms,
                        "plural forms of " + unit + " in lang/" + language + ".json");
            }
        }
    }

    @Test
    void translationsStayWithinTheEnglishWidthBudget() throws IOException {
        JsonObject reference = lang(SupportedLanguages.DEFAULT);

        List<String> violations = new ArrayList<>();
        for (String language : SupportedLanguages.codes()) {
            JsonObject actual = GSON.fromJson(
                    Files.readString(langFile(language), StandardCharsets.UTF_8),
                    JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : actual.entrySet()) {
                String key = entry.getKey();
                String englishKey = reference.has(key) || !isPluralForm(key)
                        ? key
                        : key.substring(0, key.lastIndexOf('.')) + ".other";
                int englishLength = reference.get(englishKey).getAsString().length();
                int length = entry.getValue().getAsString().length();
                int budget = Math.max(englishLength * 2, englishLength + 8);
                if (length > budget) {
                    violations.add(language + "/" + entry.getKey()
                            + " is " + length + " chars, english is " + englishLength);
                }
            }
        }
        assertTrue(violations.isEmpty(),
                "translations far longer than english risk overflowing the textures: " + violations);
    }
}
