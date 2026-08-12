package mt.server;

import io.netty.buffer.Unpooled;
import mt.client.service.UserContentLoader;
import mt.common.SupportedLanguages;
import mt.network.packet.UserContentPacket;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomSlideDeliveryTest {

    private static final List<String> CATEGORIES =
            List.of("facts", "lore", "nightmares", "special", "surreal");

    @TempDir
    Path gameDir;

    private String previousUserDir;

    @BeforeEach
    void redirectGameDirectory() {
        previousUserDir = System.getProperty("user.dir");
        System.setProperty("user.dir", gameDir.toString());
        UserContentInitializer.invalidateCache();
    }

    @AfterEach
    void restoreGameDirectory() {
        UserContentInitializer.invalidateCache();
        System.setProperty("user.dir", previousUserDir);
    }

    private Path dreamsDir() {
        return gameDir.resolve("config").resolve("midnightthoughts").resolve("dreams");
    }

    private void writeCustomEntries(String language, String category, List<String> texts) throws IOException {
        Path file = dreamsDir().resolve(language).resolve(category + ".json");
        Files.createDirectories(file.getParent());
        StringBuilder json = new StringBuilder("{\"entries\":[");
        for (int i = 0; i < texts.size(); i++) {
            if (i > 0) json.append(',');
            json.append("{\"text\":\"").append(texts.get(i).replace("\\", "\\\\").replace("\"", "\\\"")).append("\"}");
        }
        json.append("]}");
        Files.writeString(file, json.toString(), StandardCharsets.UTF_8);
    }

    private static String expected(String language, String category, int index) {
        return language + " " + category + " slide " + index;
    }

    private void writeFullLibrary() throws IOException {
        for (String language : SupportedLanguages.codes()) {
            for (String category : CATEGORIES) {
                List<String> texts = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    texts.add(expected(language, category, i));
                }
                writeCustomEntries(language, category, texts);
            }
        }
    }

    private static UserContentPacket overTheWire(UserContentPacket packet) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), RegistryAccess.EMPTY);
        UserContentPacket.CODEC.encode(buf, packet);
        UserContentPacket decoded = UserContentPacket.CODEC.decode(buf);
        assertEquals(0, buf.readableBytes(), "the user content packet was not fully consumed");
        return decoded;
    }

    private UserContentLoader connectedClient() {
        UserContentLoader loader = new UserContentLoader();
        loader.applyServerContent(overTheWire(UserContentInitializer.buildPacket()).content());
        return loader;
    }

    @Test
    void everyLanguageReceivesItsOwnCustomSlides() throws IOException {
        writeFullLibrary();

        UserContentLoader loader = connectedClient();

        for (String language : SupportedLanguages.codes()) {
            for (String category : CATEGORIES) {
                List<String> entries = loader.loadEntries(language, category);
                assertEquals(3, entries.size(),
                        "wrong entry count for " + language + "/" + category);
                for (int i = 0; i < 3; i++) {
                    assertEquals(expected(language, category, i), entries.get(i),
                            "wrong slide at index " + i + " for " + language + "/" + category);
                }
            }
        }
    }

    @Test
    void slidesNeverLeakBetweenLanguages() throws IOException {
        writeFullLibrary();

        UserContentLoader loader = connectedClient();

        for (String language : SupportedLanguages.codes()) {
            for (String category : CATEGORIES) {
                for (String text : loader.loadEntries(language, category)) {
                    assertTrue(text.startsWith(language + " "),
                            "slide \"" + text + "\" leaked into " + language + "/" + category);
                }
            }
        }
    }

    @Test
    void slidesNeverLeakBetweenCategories() throws IOException {
        writeFullLibrary();

        UserContentLoader loader = connectedClient();

        for (String language : SupportedLanguages.codes()) {
            for (String category : CATEGORIES) {
                for (String text : loader.loadEntries(language, category)) {
                    assertTrue(text.contains(" " + category + " "),
                            "slide \"" + text + "\" landed in the wrong category " + category);
                }
            }
        }
    }

    @Test
    void aSingleTranslatedLanguageIsDeliveredWithoutTheOthers() throws IOException {
        writeCustomEntries("ru_ru", "facts", List.of("Кастомный факт", "Второй факт"));

        UserContentLoader loader = connectedClient();

        assertEquals(List.of("Кастомный факт", "Второй факт"), loader.loadEntries("ru_ru", "facts"));
        for (String language : SupportedLanguages.codes()) {
            if (language.equals("ru_ru")) continue;
            assertTrue(loader.loadEntries(language, "facts").isEmpty(),
                    language + " received content it was not given");
        }
    }

    @Test
    void nonLatinSlidesSurviveTheWholeTrip() throws IOException {
        writeCustomEntries("zh_cn", "lore", List.of("这是一个梦", "夜晚记得一切"));
        writeCustomEntries("ja_jp", "surreal", List.of("これは夢です"));
        writeCustomEntries("ko_kr", "nightmares", List.of("깨어나라"));
        writeCustomEntries("uk_ua", "special", List.of("Ніч памʼятає все"));

        UserContentLoader loader = connectedClient();

        assertEquals(List.of("这是一个梦", "夜晚记得一切"), loader.loadEntries("zh_cn", "lore"));
        assertEquals(List.of("これは夢です"), loader.loadEntries("ja_jp", "surreal"));
        assertEquals(List.of("깨어나라"), loader.loadEntries("ko_kr", "nightmares"));
        assertEquals(List.of("Ніч памʼятає все"), loader.loadEntries("uk_ua", "special"));
    }

    @Test
    void aCategoryWithoutCustomContentStaysEmpty() throws IOException {
        writeCustomEntries("en_us", "facts", List.of("only facts"));

        UserContentLoader loader = connectedClient();

        assertEquals(List.of("only facts"), loader.loadEntries("en_us", "facts"));
        assertTrue(loader.loadEntries("en_us", "lore").isEmpty());
        assertTrue(loader.loadEntries("en_us", "nightmares").isEmpty());
    }

    @Test
    void anEmptyServerLibraryDeliversNothing() throws IOException {
        UserContentInitializer.writeDefaultFiles();

        UserContentLoader loader = connectedClient();

        for (String language : SupportedLanguages.codes()) {
            for (String category : CATEGORIES) {
                assertTrue(loader.loadEntries(language, category).isEmpty(),
                        "empty server library produced content for " + language + "/" + category);
            }
        }
    }

    @Test
    void slideOrderIsPreservedForEveryLanguage() throws IOException {
        for (String language : SupportedLanguages.codes()) {
            List<String> ordered = new ArrayList<>();
            for (int i = 0; i < 25; i++) {
                ordered.add(language + " ordered " + i);
            }
            writeCustomEntries(language, "facts", ordered);
        }

        UserContentLoader loader = connectedClient();

        for (String language : SupportedLanguages.codes()) {
            List<String> entries = loader.loadEntries(language, "facts");
            for (int i = 0; i < 25; i++) {
                assertEquals(language + " ordered " + i, entries.get(i),
                        "slide order changed for " + language + " at index " + i);
            }
        }
    }

    @Test
    void serverContentWinsUntilTheClientDisconnects() throws IOException {
        writeCustomEntries("de_de", "facts", List.of("vom Server"));
        UserContentLoader loader = connectedClient();

        writeCustomEntries("de_de", "facts", List.of("lokal"));

        assertEquals(List.of("vom Server"), loader.loadEntries("de_de", "facts"),
                "while connected the server library must win over local files");

        loader.clearServerContent();

        assertEquals(List.of("lokal"), loader.loadEntries("de_de", "facts"),
                "after disconnecting the client must read its own files");
    }

    @Test
    void aReloadDeliversEditedSlidesToTheClient() throws IOException {
        writeCustomEntries("en_us", "facts", List.of("before reload"));
        UserContentLoader loader = connectedClient();
        assertEquals(List.of("before reload"), loader.loadEntries("en_us", "facts"));

        writeCustomEntries("en_us", "facts", List.of("after reload"));
        UserContentInitializer.invalidateCache();
        loader.applyServerContent(overTheWire(UserContentInitializer.buildPacket()).content());

        assertEquals(List.of("after reload"), loader.loadEntries("en_us", "facts"));
    }

    @Test
    void oversizedSlidesNeverReachTheClient() throws IOException {
        String oversized = "x".repeat(UserContentInitializer.MAX_ENTRY_LENGTH + 1);
        writeCustomEntries("en_us", "facts", List.of("kept", oversized));

        UserContentLoader loader = connectedClient();

        assertEquals(List.of("kept"), loader.loadEntries("en_us", "facts"));
    }

    @Test
    void aBrokenFileForOneLanguageDoesNotAffectTheOthers() throws IOException {
        writeFullLibrary();
        Path broken = dreamsDir().resolve("fr_fr").resolve("facts.json");
        Files.writeString(broken, "{ broken ");

        UserContentLoader loader = connectedClient();

        assertTrue(loader.loadEntries("fr_fr", "facts").isEmpty());
        assertEquals(3, loader.loadEntries("fr_fr", "lore").size());
        for (String language : SupportedLanguages.codes()) {
            if (language.equals("fr_fr")) continue;
            assertEquals(3, loader.loadEntries(language, "facts").size(),
                    language + " lost its content because another language was broken");
        }
    }
}
