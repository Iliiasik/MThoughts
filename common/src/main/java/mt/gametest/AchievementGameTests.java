package mt.gametest;

import mt.gametest.TestContext.Bed;
import mt.network.packet.DailySummaryPacket;
import mt.server.AchievementLoader;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

final class AchievementGameTests {

    private static final String ID = "achievements";

    private AchievementGameTests() {}

    static List<String> achievements(TestContext ctx, MockPlayer viewer, MockPlayer subject) {
        DailySummaryPacket summary = ctx.lastSummary(viewer);
        return ctx.summaryLine(summary, subject).achievements();
    }

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "Achievements", List.of(
                "personal records are announced from the second night on, only when beaten",
                "a custom achievement is given exactly when its conditions hold and only once",
                "/midnightthoughts reload achievements makes new rules count on the next night")));

        cases.add(new GameTestCase(ID, "records_from_the_second_night", "Personal records are announced from the second night on", 1200, ctx -> {
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Walker", bed);
            player.get().awardStat(Stats.WALK_ONE_CM, 1000);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the first night", player)
                    .then(() -> {
                        ctx.equal(List.of(), achievements(ctx, player, player), "achievements on the first night");
                        player.get().awardStat(Stats.WALK_ONE_CM, 2000);
                        player.get().awardStat(Stats.BLOCK_MINED.get(Blocks.STONE), 3);
                        ctx.night();
                    })
                    .idle(2)
                    .then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the second night", player)
                    .then(() -> {
                        List<String> got = achievements(ctx, player, player);
                        ctx.check(got.contains("distance_record"), "no distance record: " + got);
                        ctx.check(got.contains("blocks_record"), "no blocks record: " + got);
                        ctx.check(!got.contains("mobs_record"), "a mobs record without any kills: " + got);
                    });
        }));

        cases.add(new GameTestCase(ID, "custom_conditions", "A custom achievement is given only when its conditions hold", 600, ctx -> {
            CommandGameTests.writeAchievements("{\"achievements\":["
                    + "{\"id\":\"jumper\",\"name\":\"Jumper\",\"conditions\":{\"jumpsMin\":10}},"
                    + "{\"id\":\"pacifist\",\"name\":\"Pacifist\",\"conditions\":{\"mobsKilledEq\":0,\"deathsEq\":0}}]}");
            AchievementLoader.reload();
            Bed first = SleepGameTests.litBed(ctx, 4);
            Bed second = SleepGameTests.litBed(ctx, 16);
            MockPlayer hopper = ctx.joinAt("Hopper", first);
            MockPlayer hunter = ctx.joinAt("Hunter", second);
            hopper.get().awardStat(Stats.JUMP, 15);
            hunter.get().awardStat(Stats.JUMP, 5);
            hunter.get().awardStat(Stats.MOB_KILLS, 1);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(hopper, first);
                ctx.sleepOrFail(hunter, second);
            }).sleepThroughTheNight("the night to pass", hopper).then(() -> {
                List<String> hopperGot = achievements(ctx, hopper, hopper);
                List<String> hunterGot = achievements(ctx, hopper, hunter);
                ctx.check(hopperGot.contains("jumper") && hopperGot.contains("pacifist"), "Hopper achievements: " + hopperGot);
                ctx.check(!hunterGot.contains("jumper") && !hunterGot.contains("pacifist"), "Hunter achievements: " + hunterGot);
            });
        }));

        cases.add(new GameTestCase(ID, "custom_given_once", "A custom achievement is announced only once", 1200, ctx -> {
            CommandGameTests.writeAchievements("{\"achievements\":[{\"id\":\"jumper\",\"name\":\"Jumper\",\"conditions\":{\"jumpsMin\":10}}]}");
            AchievementLoader.reload();
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Hopper", bed);
            player.get().awardStat(Stats.JUMP, 15);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the first night", player)
                    .then(() -> {
                        ctx.check(achievements(ctx, player, player).contains("jumper"), "no achievement on the first night");
                        player.get().awardStat(Stats.JUMP, 15);
                        ctx.night();
                    })
                    .idle(2)
                    .then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the second night", player)
                    .then(() -> ctx.check(!achievements(ctx, player, player).contains("jumper"), "the achievement was announced twice"));
        }));

        cases.add(new GameTestCase(ID, "reload_adds_rules", "A rule added with reload achievements counts on the next night", 600, ctx -> {
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Learner", bed);
            player.get().awardStat(Stats.JUMP, 12);
            ctx.then(() -> {
                CommandGameTests.writeAchievements("{\"achievements\":[{\"id\":\"springy\",\"name\":\"Springy\",\"conditions\":{\"jumpsMin\":10}}]}");
                ctx.check(ctx.op("midnightthoughts reload achievements").result() > 0, "the reload command failed");
                ctx.night();
            }).idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the night to pass", player)
                    .then(() -> ctx.check(achievements(ctx, player, player).contains("springy"), "the new rule did not count"));
        }));
    }
}
