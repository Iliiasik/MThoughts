package mt.gametest;

import mt.common.SupportedLanguages;
import mt.config.MidnightThoughtsConfig;
import mt.network.packet.UserContentPacket;
import mt.server.UserContentInitializer;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

final class ContentGameTests {

    private static final String ID = "content";
    private static final List<String> CATEGORIES = List.of("facts", "lore", "nightmares", "special", "surreal");

    private ContentGameTests() {}

    static Path dreams(String language, String category) {
        return MidnightThoughtsConfig.getConfigDir().resolve("dreams").resolve(language).resolve(category + ".json");
    }

    static void writeEntries(String language, String category, String... texts) {
        StringBuilder json = new StringBuilder("{\"entries\":[");
        for (int i = 0; i < texts.length; i++) {
            if (i > 0) json.append(',');
            json.append("{\"text\":\"").append(texts[i].replace("\\", "\\\\").replace("\"", "\\\"")).append("\"}");
        }
        json.append("]}");
        FileGameTests.write(dreams(language, category), json.toString());
    }

    static Map<String, List<String>> lastContent(TestContext ctx, MockPlayer player) {
        return ctx.lastSent(player, UserContentPacket.class).content();
    }

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "Custom dream content", List.of(
                "custom slides are sent to players when they join and again after /midnightthoughts reload content",
                "every language and every category, nightmares included, reach the player intact",
                "oversized entries are dropped and one broken file does not affect the others")));

        cases.add(new GameTestCase(ID, "sent_on_join", "Custom slides are sent to a player who joins", 100, ctx -> {
            writeEntries("en_us", "facts", "Owls dream in slow motion");
            UserContentInitializer.invalidateCache();
            MockPlayer player = ctx.join("Reader");
            ctx.then(() -> ctx.equal(List.of("Owls dream in slow motion"), lastContent(ctx, player).get("en_us:facts"),
                    "english facts sent on join"));
        }));

        cases.add(new GameTestCase(ID, "resent_after_reload", "Edited slides are sent again after reload content", 100, ctx -> {
            MockPlayer player = ctx.join("Reader");
            ctx.then(() -> {
                writeEntries("de_de", "lore", "Ein alter Turm erinnert sich");
                ctx.check(ctx.op("midnightthoughts reload content").result() > 0, "the reload command failed");
                ctx.equal(List.of("Ein alter Turm erinnert sich"), lastContent(ctx, player).get("de_de:lore"),
                        "german lore after the reload");
            });
        }));

        cases.add(new GameTestCase(ID, "every_language_and_category", "Every language and category reaches the player intact", 100, ctx -> {
            MockPlayer player = ctx.join("Reader");
            ctx.then(() -> {
                for (String language : SupportedLanguages.codes()) {
                    for (String category : CATEGORIES) {
                        writeEntries(language, category, language + " " + category + " ✓ «тест» 夢");
                    }
                }
                ctx.op("midnightthoughts reload content");
                Map<String, List<String>> content = lastContent(ctx, player);
                ctx.equal(SupportedLanguages.codes().size() * CATEGORIES.size(), content.size(), "language and category pairs sent");
                for (String language : SupportedLanguages.codes()) {
                    for (String category : CATEGORIES) {
                        ctx.equal(List.of(language + " " + category + " ✓ «тест» 夢"), content.get(language + ":" + category),
                                "slides for " + language + "/" + category);
                    }
                }
            });
        }));

        cases.add(new GameTestCase(ID, "oversized_entries_dropped", "Entries longer than the limit are not sent", 100, ctx -> {
            MockPlayer player = ctx.join("Reader");
            ctx.then(() -> {
                writeEntries("en_us", "surreal", "x".repeat(UserContentInitializer.MAX_ENTRY_LENGTH + 1), "A short dream");
                ctx.op("midnightthoughts reload content");
                ctx.equal(List.of("A short dream"), lastContent(ctx, player).get("en_us:surreal"), "surreal slides sent");
            });
        }));

        cases.add(new GameTestCase(ID, "broken_file_isolated", "A broken file does not affect the other files", 100, ctx -> {
            MockPlayer player = ctx.join("Reader");
            ctx.then(() -> {
                FileGameTests.write(dreams("en_us", "nightmares"), "{ broken");
                writeEntries("ru_ru", "nightmares", "Тени шепчут под кроватью");
                ctx.op("midnightthoughts reload content");
                Map<String, List<String>> content = lastContent(ctx, player);
                ctx.check(!content.containsKey("en_us:nightmares"), "the broken english file produced slides");
                ctx.equal(List.of("Тени шепчут под кроватью"), content.get("ru_ru:nightmares"), "russian nightmares sent");
            });
        }));
    }
}
