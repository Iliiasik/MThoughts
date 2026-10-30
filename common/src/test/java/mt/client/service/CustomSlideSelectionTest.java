package mt.client.service;

import mt.cache.ServerConfigCache;
import mt.client.api.UselessFactsApiClient;
import mt.client.model.Slide;
import mt.client.model.SlideCategory;
import mt.client.repository.SlideRepository;
import mt.common.SupportedLanguages;
import mt.config.MidnightThoughtsConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomSlideSelectionTest {

    private static final Map<SlideCategory, String> FILES = Map.of(
            SlideCategory.LORE, "lore",
            SlideCategory.SURREAL, "surreal",
            SlideCategory.SPECIAL, "special",
            SlideCategory.NIGHTMARE, "nightmares");

    private static final int DRAWS = 200;

    @TempDir
    Path gameDir;

    private String previousUserDir;

    @BeforeEach
    void redirectGameDirectory() {
        previousUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", gameDir.toString());
        ServerConfigCache.clear();
        MidnightThoughtsConfig.reload();
    }

    @AfterEach
    void restoreGameDirectory() {
        System.setProperty("user.dir", previousUserDir);
        MidnightThoughtsConfig.reload();
    }

    private static String custom(String language, String file) {
        return "custom " + language + " " + file;
    }

    private static FactProvider providerWith(Map<String, List<String>> content) {
        UserContentLoader loader = new UserContentLoader();
        loader.applyServerContent(content);
        return new FactProvider(new UselessFactsApiClient(), new SlideRepository(), loader);
    }

    private static Map<String, List<String>> everything() {
        Map<String, List<String>> content = new HashMap<>();
        for (String language : SupportedLanguages.codes()) {
            content.put(language + ":facts", List.of(custom(language, "facts")));
            for (String file : FILES.values()) {
                content.put(language + ":" + file, List.of(custom(language, file)));
            }
        }
        return content;
    }

    private static void replaceBuiltInSlides(boolean replace) {
        MidnightThoughtsConfig.getInstance().getSleepOverlay().userContentReplaces = replace;
    }

    @Test
    void replaceModeShowsOnlyCustomSlidesInEveryLanguage() {
        replaceBuiltInSlides(true);
        FactProvider provider = providerWith(everything());

        for (String language : SupportedLanguages.codes()) {
            for (int i = 0; i < 20; i++) {
                assertEquals(custom(language, "facts"), provider.getNextFact(language).text(),
                        "facts in " + language);
                for (Map.Entry<SlideCategory, String> category : FILES.entrySet()) {
                    Slide slide = provider.getNextForCategory(language, category.getKey());
                    assertNotNull(slide, category.getValue() + " in " + language + " fell back to built-in slides");
                    assertEquals(custom(language, category.getValue()), slide.text(), category.getValue() + " in " + language);
                    assertEquals(category.getKey(), slide.category(), "category of the custom slide");
                }
            }
        }
    }

    @Test
    void mixModeShowsCustomAndBuiltInSlides() {
        replaceBuiltInSlides(false);
        FactProvider provider = providerWith(everything());

        for (Map.Entry<SlideCategory, String> category : FILES.entrySet()) {
            int custom = 0;
            int builtIn = 0;
            for (int i = 0; i < DRAWS; i++) {
                if (provider.getNextForCategory("en_us", category.getKey()) == null) builtIn++; else custom++;
            }
            assertTrue(custom > 0 && builtIn > 0,
                    category.getValue() + " did not mix: " + custom + " custom, " + builtIn + " built-in");
        }
    }

    @Test
    void onlyTheCategoriesWithCustomFilesAreReplaced() {
        replaceBuiltInSlides(true);
        Map<String, List<String>> content = Map.of("en_us:lore", List.of(custom("en_us", "lore")));
        FactProvider provider = providerWith(content);

        for (int i = 0; i < 20; i++) {
            assertEquals(custom("en_us", "lore"), provider.getNextForCategory("en_us", SlideCategory.LORE).text(), "lore");
            assertNull(provider.getNextForCategory("en_us", SlideCategory.NIGHTMARE), "nightmares without a custom file");
            assertNull(provider.getNextForCategory("en_us", SlideCategory.SURREAL), "surreal without a custom file");
        }
    }

    @Test
    void customSlidesOfOneLanguageNeverShowInAnother() {
        replaceBuiltInSlides(true);
        Map<String, List<String>> content = Map.of("ru_ru:nightmares", List.of(custom("ru_ru", "nightmares")));
        FactProvider provider = providerWith(content);

        assertEquals(custom("ru_ru", "nightmares"), provider.getNextForCategory("ru_ru", SlideCategory.NIGHTMARE).text(),
                "russian nightmares");
        for (String language : SupportedLanguages.codes()) {
            if (language.equals("ru_ru")) continue;
            assertNull(provider.getNextForCategory(language, SlideCategory.NIGHTMARE), "nightmares leaked into " + language);
        }
    }

    @Test
    void nightmaresAreReadFromTheNightmaresFile() {
        replaceBuiltInSlides(true);
        FactProvider provider = providerWith(Map.of("de_de:nightmares", List.of("Schatten unter dem Bett")));

        Slide slide = provider.getNextForCategory("de_de", SlideCategory.NIGHTMARE);
        assertNotNull(slide, "the nightmares file was not used");
        assertEquals("Schatten unter dem Bett", slide.text(), "german nightmare");
    }
}
