package mt.gametest;

import mt.config.MidnightThoughtsConfig;
import mt.gametest.TestContext.Bed;
import mt.gametest.TestContext.Decor;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.server.WellRestedEffect;

import java.util.List;

final class CommandGameTests {

    private static final String ID = "commands";

    private CommandGameTests() {}

    static void writeAchievements(String json) {
        FileGameTests.write(MidnightThoughtsConfig.getConfigDir().resolve("achievements.json"), json);
    }

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "Commands", List.of(
                "/midnightthoughts reload, reload all, reload config, reload achievements and reload content",
                "/midnightthoughts comfort for yourself and for another player",
                "/midnightthoughts wellrested grant, clear and info, including bad levels and a disabled feature",
                "players without operator rights cannot run any of them")));

        cases.add(new GameTestCase(ID, "reload_everything", "reload and reload all resend config, achievements and content", 100, ctx -> {
            MockPlayer player = ctx.join("Admin");
            ctx.then(() -> {
                for (String command : List.of("midnightthoughts reload", "midnightthoughts reload all")) {
                    int configs = ctx.sent(player, SyncConfigPacket.class).size();
                    int achievements = ctx.sent(player, SyncAchievementsPacket.class).size();
                    int content = ctx.sent(player, UserContentPacket.class).size();
                    TestContext.CommandResult result = ctx.op(command);
                    ctx.check(result.result() > 0, command + " failed");
                    ctx.check(result.mentions("Reloaded config"), command + " did not report back");
                    ctx.check(ctx.sent(player, SyncConfigPacket.class).size() > configs, command + " did not resend the config");
                    ctx.check(ctx.sent(player, SyncAchievementsPacket.class).size() > achievements, command + " did not resend achievements");
                    ctx.check(ctx.sent(player, UserContentPacket.class).size() > content, command + " did not resend content");
                }
            });
        }));

        cases.add(new GameTestCase(ID, "reload_achievements", "reload achievements loads new rules and reports the count", 100, ctx -> {
            MockPlayer player = ctx.join("Admin");
            ctx.then(() -> {
                writeAchievements("{\"achievements\":[{\"id\":\"one\",\"name\":\"One\"},{\"id\":\"two\",\"name\":\"Two\"}]}");
                TestContext.CommandResult result = ctx.op("midnightthoughts reload achievements");
                ctx.check(result.mentions("(2)"), "the command did not report 2 achievements: " + result.output());
                ctx.equal(2, ctx.lastSent(player, SyncAchievementsPacket.class).achievements().size(), "achievements sent to the player");
            });
        }));

        cases.add(new GameTestCase(ID, "comfort_report", "comfort reports for yourself and for another player", 100, ctx -> {
            Bed bed = ctx.bed(10, 10);
            ctx.decorate(bed, Decor.LIGHTING);
            MockPlayer player = ctx.joinAt("Inspector", bed);
            ctx.then(() -> {
                TestContext.CommandResult self = ctx.opAs(player, "midnightthoughts comfort");
                ctx.check(self.result() > 0, "comfort for yourself failed");
                ctx.check(self.mentions("Comfort Report: Inspector"), "the report has no header: " + self.output());
                ctx.check(self.mentions("Total comfort"), "the report has no total");
                TestContext.CommandResult other = ctx.op("midnightthoughts comfort Inspector");
                ctx.check(other.result() > 0 && other.mentions("Comfort Report: Inspector"), "comfort for another player failed");
            });
        }));

        cases.add(new GameTestCase(ID, "wellrested_grant", "wellrested grant gives the level and rejects bad levels", 100, ctx -> {
            MockPlayer player = ctx.join("Granted");
            ctx.then(() -> {
                ctx.check(ctx.op("midnightthoughts wellrested grant Granted 3").result() > 0, "granting level 3 failed");
                ctx.equal(3, WellRestedEffect.getLevel(player.get()), "level after granting 3");
                ctx.equal(-1, ctx.op("midnightthoughts wellrested grant Granted 6").result(), "level 6 was accepted");
                ctx.equal(-1, ctx.op("midnightthoughts wellrested grant Granted 0").result(), "level 0 was accepted");
                ctx.equal(3, WellRestedEffect.getLevel(player.get()), "level after the rejected grants");
            });
        }));

        cases.add(new GameTestCase(ID, "wellrested_grant_disabled", "wellrested grant refuses while Well Rested is off", 100, ctx -> {
            ctx.config().getWellRested().enabled = false;
            MockPlayer player = ctx.join("Refused");
            ctx.then(() -> {
                TestContext.CommandResult result = ctx.op("midnightthoughts wellrested grant Refused 2");
                ctx.equal(0, result.result(), "result while switched off");
                ctx.check(result.mentions("disabled"), "the command did not explain why: " + result.output());
                ctx.check(!WellRestedEffect.hasEffect(player.get()), "an effect was granted while switched off");
            });
        }));

        cases.add(new GameTestCase(ID, "wellrested_clear_and_info", "wellrested info and clear report and remove the effect", 100, ctx -> {
            MockPlayer player = ctx.join("Informed");
            ctx.then(() -> {
                ctx.op("midnightthoughts wellrested grant Informed 2");
                TestContext.CommandResult info = ctx.op("midnightthoughts wellrested info Informed");
                ctx.check(info.mentions("Level: 2") && info.mentions("Phase: 1 / 3"), "info did not describe the effect: " + info.output());
                ctx.check(ctx.op("midnightthoughts wellrested clear Informed").result() > 0, "clear failed");
                ctx.check(!WellRestedEffect.hasEffect(player.get()), "clear left the effect");
                ctx.check(ctx.opAs(player, "midnightthoughts wellrested info").mentions("No active effect"), "info after clearing");
            });
        }));

        cases.add(new GameTestCase(ID, "operators_only", "Players without operator rights cannot run the commands", 100, ctx -> {
            MockPlayer player = ctx.join("Visitor");
            ctx.then(() -> {
                for (String command : List.of("midnightthoughts reload", "midnightthoughts comfort",
                        "midnightthoughts wellrested grant Visitor 5", "midnightthoughts config recreate")) {
                    ctx.equal(-1, ctx.asPlayer(player, command).result(), "result of " + command + " without rights");
                }
                ctx.check(!WellRestedEffect.hasEffect(player.get()), "a player without rights granted an effect");
            });
        }));
    }
}
