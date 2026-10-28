package mt.gametest;

import mt.config.MidnightThoughtsConfig;
import mt.gametest.TestContext.Bed;
import mt.network.packet.SyncConfigPacket;
import mt.server.StatsStorage;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SuppressWarnings("resource")
final class FileGameTests {

    private static final String ID = "files";

    private FileGameTests() {}

    static Path configFile() {
        return MidnightThoughtsConfig.getConfigDir().resolve("midnightthoughts.json");
    }

    static void write(Path path, String text) {
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, text, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    static String read(Path path) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    static void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "Files", List.of(
                "a deleted config is recreated with defaults by /midnightthoughts reload config",
                "an edited config is applied on reload and sent to every player",
                "a broken config is backed up and replaced, /midnightthoughts config recreate keeps a backup",
                "the per player stats file survives relogging and a broken stats file is backed up")));

        cases.add(new GameTestCase(ID, "deleted_config_recreated", "A deleted config is recreated with defaults on reload", 100, ctx -> {
            MockPlayer player = ctx.join("Admin");
            ctx.then(() -> {
                delete(configFile());
                ctx.check(ctx.op("midnightthoughts reload config").result() > 0, "the reload command failed");
                ctx.check(Files.exists(configFile()), "the config file was not recreated");
                ctx.equal(5, ctx.config().getComfort().scanRadius, "scan radius after recreating");
                ctx.check(!ctx.sent(player, SyncConfigPacket.class).isEmpty(), "the config was not sent to the player");
            });
        }));

        cases.add(new GameTestCase(ID, "edited_config_applied", "An edited config is applied on reload and sent to players", 100, ctx -> {
            MockPlayer player = ctx.join("Admin");
            ctx.then(() -> {
                write(configFile(), "{\"comfort\":{\"scanRadius\":3},\"ui\":{\"theme\":\"magic\"}}");
                ctx.check(ctx.op("midnightthoughts reload config").result() > 0, "the reload command failed");
                ctx.equal(3, ctx.config().getComfort().scanRadius, "scan radius after the edit");
                ctx.equal("magic", ctx.lastSent(player, SyncConfigPacket.class).theme(), "theme sent to the player");
            });
        }));

        cases.add(new GameTestCase(ID, "broken_config_backed_up", "A broken config is backed up and replaced with defaults", 100, ctx -> {
            ctx.join("Admin");
            ctx.then(() -> {
                write(configFile(), "{ this is not json");
                ctx.check(ctx.op("midnightthoughts reload config").result() > 0, "the reload command failed");
                ctx.check(Files.exists(configFile().resolveSibling("midnightthoughts.json.broken")), "the broken config was not kept");
                ctx.equal(5, ctx.config().getComfort().scanRadius, "scan radius after the broken config");
            });
        }));

        cases.add(new GameTestCase(ID, "recreate_keeps_a_backup", "/midnightthoughts config recreate keeps a backup", 100, ctx -> {
            MockPlayer player = ctx.join("Admin");
            ctx.then(() -> {
                write(configFile(), "{\"comfort\":{\"scanRadius\":7}}");
                ctx.op("midnightthoughts reload config");
                ctx.equal(7, ctx.config().getComfort().scanRadius, "scan radius before recreating");
                int synced = ctx.sent(player, SyncConfigPacket.class).size();
                TestContext.CommandResult result = ctx.op("midnightthoughts config recreate");
                ctx.check(result.result() > 0, "the recreate command failed");
                Path backup = configFile().resolveSibling("midnightthoughts.json.backup");
                ctx.check(Files.exists(backup), "no backup was made");
                ctx.check(read(backup).contains("\"scanRadius\": 7"), "the backup does not hold the old config");
                ctx.equal(5, ctx.config().getComfort().scanRadius, "scan radius after recreating");
                ctx.check(ctx.sent(player, SyncConfigPacket.class).size() > synced, "the new config was not sent");
            });
        }));

        cases.add(new GameTestCase(ID, "stats_survive_relogging", "Stats since the last sleep survive relogging", 1200, ctx -> {
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer[] player = {ctx.joinAt("Returning", bed)};
            player[0].get().awardStat(Stats.JUMP, 20);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player[0], bed))
                    .sleepThroughTheNight("the first night", player[0])
                    .then(() -> {
                        ctx.equal(20, ctx.summaryLine(ctx.lastSummary(player[0]), player[0]).jumps(), "jumps on the first night");
                        player[0].get().awardStat(Stats.JUMP, 5);
                        player[0] = ctx.rejoin(player[0], bed.standingSpot());
                        ctx.night();
                    })
                    .idle(2)
                    .then(() -> ctx.sleepOrFail(player[0], bed))
                    .sleepThroughTheNight("the second night", player[0])
                    .then(() -> ctx.equal(5, ctx.summaryLine(ctx.lastSummary(player[0]), player[0]).jumps(), "jumps after relogging"));
        }));

        cases.add(new GameTestCase(ID, "broken_stats_file", "A broken stats file is backed up and sleeping still works", 700, ctx -> {
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Sleeper", bed);
            Path stats = ctx.server().getWorldPath(LevelResource.ROOT).resolve("midnightthoughts_stats.json");
            ctx.then(() -> {
                StatsStorage.unload();
                write(stats, "[[[ not stats");
                ctx.night();
            }).idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the night to pass", player)
                    .then(() -> {
                        SleepGameTests.expectSummary(ctx, player, 1);
                        ctx.check(Files.exists(stats.resolveSibling("midnightthoughts_stats.json.broken")), "the broken stats file was not kept");
                    });
        }));
    }
}
