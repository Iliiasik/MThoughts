package mt.server;

import io.netty.buffer.Unpooled;
import mt.common.SupportedLanguages;
import mt.network.packet.UserContentPacket;
import net.minecraft.network.FriendlyByteBuf;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserContentInitializerTest {

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

    private void writeEntries(String language, String category, List<String> texts) throws IOException {
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

    @Test
    void writeDefaultFilesCoversEveryLanguageAndCategory() {
        UserContentInitializer.writeDefaultFiles();

        for (String language : SupportedLanguages.codes()) {
            for (String category : List.of("facts", "lore", "nightmares", "special", "surreal")) {
                assertTrue(Files.isRegularFile(dreamsDir().resolve(language).resolve(category + ".json")),
                        "missing default file for " + language + "/" + category);
            }
        }
    }

    @Test
    void writeDefaultFilesDoesNotOverwriteExistingContent() throws IOException {
        writeEntries("en_us", "facts", List.of("kept"));

        UserContentInitializer.writeDefaultFiles();

        assertEquals(List.of("kept"), UserContentInitializer.buildPacket().content().get("en_us:facts"));
    }

    @Test
    void anEmptyInstallProducesAnEmptyPacket() {
        UserContentInitializer.writeDefaultFiles();

        assertTrue(UserContentInitializer.buildPacket().content().isEmpty(),
                "empty category files must not be sent to players");
    }

    @Test
    void entriesAreKeyedByLanguageAndCategory() throws IOException {
        writeEntries("ru_ru", "lore", List.of("первая", "вторая"));

        UserContentPacket packet = UserContentInitializer.buildPacket();

        assertEquals(List.of("первая", "вторая"), packet.content().get("ru_ru:lore"));
        assertEquals(1, packet.content().size());
    }

    @Test
    void blankEntriesAreDropped() throws IOException {
        writeEntries("en_us", "facts", List.of("real", "   ", ""));

        assertEquals(List.of("real"), UserContentInitializer.buildPacket().content().get("en_us:facts"));
    }

    @Test
    void thePacketIsBuiltOnceAndReused() throws IOException {
        writeEntries("en_us", "facts", List.of("cached"));

        UserContentPacket first = UserContentInitializer.buildPacket();
        UserContentPacket second = UserContentInitializer.buildPacket();

        assertSame(first, second, "the packet is rebuilt from disk on every call");
    }

    @Test
    void theCacheIsNotRefreshedUntilInvalidated() throws IOException {
        writeEntries("en_us", "facts", List.of("before"));
        UserContentInitializer.buildPacket();

        writeEntries("en_us", "facts", List.of("after"));

        assertEquals(List.of("before"), UserContentInitializer.buildPacket().content().get("en_us:facts"));
    }

    @Test
    void invalidatingTheCacheRereadsTheFiles() throws IOException {
        writeEntries("en_us", "facts", List.of("before"));
        UserContentPacket first = UserContentInitializer.buildPacket();

        writeEntries("en_us", "facts", List.of("after"));
        UserContentInitializer.invalidateCache();
        UserContentPacket second = UserContentInitializer.buildPacket();

        assertNotSame(first, second);
        assertEquals(List.of("after"), second.content().get("en_us:facts"));
    }

    @Test
    void anEntryLongerThanTheLimitIsSkipped() throws IOException {
        String oversized = "x".repeat(UserContentInitializer.MAX_ENTRY_LENGTH + 1);
        writeEntries("en_us", "facts", List.of("short", oversized, "also short"));

        List<String> accepted = UserContentInitializer.buildPacket().content().get("en_us:facts");

        assertEquals(List.of("short", "also short"), accepted);
    }

    @Test
    void anEntryExactlyAtTheLimitIsKept() throws IOException {
        String atLimit = "x".repeat(UserContentInitializer.MAX_ENTRY_LENGTH);
        writeEntries("en_us", "facts", List.of(atLimit));

        assertEquals(List.of(atLimit), UserContentInitializer.buildPacket().content().get("en_us:facts"));
    }

    @Test
    void anOversizedOnlyCategoryIsOmittedEntirely() throws IOException {
        writeEntries("en_us", "facts", List.of("y".repeat(UserContentInitializer.MAX_ENTRY_LENGTH + 1)));

        assertFalse(UserContentInitializer.buildPacket().content().containsKey("en_us:facts"));
    }

    private static int contentBytes(UserContentPacket packet) {
        int bytes = 0;
        for (Map.Entry<String, List<String>> entry : packet.content().entrySet()) {
            bytes += entry.getKey().getBytes(StandardCharsets.UTF_8).length;
            for (String text : entry.getValue()) {
                bytes += text.getBytes(StandardCharsets.UTF_8).length;
            }
        }
        return bytes;
    }

    private void writeOversizedLibrary(String filler) throws IOException {
        String chunk = filler.repeat(UserContentInitializer.MAX_ENTRY_LENGTH);
        int chunkBytes = chunk.getBytes(StandardCharsets.UTF_8).length;
        int enough = UserContentInitializer.MAX_PAYLOAD_BYTES / chunkBytes + 10;

        List<String> entries = new ArrayList<>(enough);
        for (int i = 0; i < enough; i++) {
            entries.add(chunk);
        }
        writeEntries(SupportedLanguages.DEFAULT, "facts", entries);
        writeEntries(SupportedLanguages.DEFAULT, "lore", entries);
    }

    @Test
    void thePayloadIsTruncatedAtTheByteBudget() throws IOException {
        writeOversizedLibrary("z");

        UserContentPacket packet = UserContentInitializer.buildPacket();

        assertTrue(contentBytes(packet) <= UserContentInitializer.MAX_PAYLOAD_BYTES,
                "payload of " + contentBytes(packet) + " bytes exceeds the budget");
        assertFalse(packet.content().isEmpty(), "the budget must not discard everything");
    }

    @Test
    void aTruncatedPayloadStillFitsInAVanillaCustomPayload() throws IOException {
        writeOversizedLibrary("я");

        UserContentPacket packet = UserContentInitializer.buildPacket();
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        UserContentPacket.encode(packet, buf);

        assertTrue(buf.readableBytes() < 1048576,
                "encoded payload of " + buf.readableBytes() + " bytes would be rejected by vanilla");
    }

    @Test
    void truncationStopsAtTheFirstCategoryThatDoesNotFit() throws IOException {
        writeOversizedLibrary("z");

        UserContentPacket packet = UserContentInitializer.buildPacket();

        assertTrue(packet.content().containsKey("en_us:facts"));
        assertFalse(packet.content().containsKey("en_us:lore"),
                "collection must stop once the budget is spent");
    }

    @Test
    void malformedJsonIsIgnoredWithoutBreakingOtherCategories() throws IOException {
        Path broken = dreamsDir().resolve("en_us").resolve("facts.json");
        Files.createDirectories(broken.getParent());
        Files.writeString(broken, "{ not json ");
        writeEntries("en_us", "lore", List.of("survives"));

        UserContentPacket packet = UserContentInitializer.buildPacket();

        assertFalse(packet.content().containsKey("en_us:facts"));
        assertEquals(List.of("survives"), packet.content().get("en_us:lore"));
    }

    @Test
    void aFileWithoutAnEntriesArrayIsIgnored() throws IOException {
        Path file = dreamsDir().resolve("en_us").resolve("facts.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{\"somethingElse\":[]}");

        assertTrue(UserContentInitializer.buildPacket().content().isEmpty());
    }

    @Test
    void entriesWithoutATextFieldAreIgnored() throws IOException {
        Path file = dreamsDir().resolve("en_us").resolve("facts.json");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{\"entries\":[{\"rarity\":1.0},{\"text\":\"kept\"},\"a bare string\"]}");

        assertEquals(List.of("kept"), UserContentInitializer.buildPacket().content().get("en_us:facts"));
    }

    @Test
    void everyLanguageCanCarryItsOwnContent() throws IOException {
        for (String language : SupportedLanguages.codes()) {
            writeEntries(language, "special", List.of(language + " entry"));
        }

        UserContentPacket packet = UserContentInitializer.buildPacket();

        assertEquals(SupportedLanguages.codes().size(), packet.content().size());
        for (String language : SupportedLanguages.codes()) {
            assertEquals(List.of(language + " entry"), packet.content().get(language + ":special"));
        }
    }
}
