package mt.gametest;

import mt.gametest.TestContext.Bed;
import mt.network.packet.DailySummaryPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

final class ExperienceGameTests {

    private static final String ID = "experience";

    private ExperienceGameTests() {}

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "Experience for the day", List.of(
                "the points behind the MVP score are turned into experience after sleeping",
                "mined blocks, distance, mob kills and jumps all count, deaths take points away but never experience",
                "only players who slept through get experience, each one exactly their own amount",
                "the rate, the limit and the switch from the config are used, fractions are rounded down",
                "the summary carries the experience and the Well Rested level of every player")));

        cases.add(new GameTestCase(ID, "mined_blocks", "500 mined blocks give 45 experience", 600, ctx -> {
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Miner", bed);
            mine(player.get(), 500);
            sleepAlone(ctx, player, bed).then(() -> {
                ctx.equal(45, player.get().totalExperience, "experience of the miner");
                ctx.equal(45, line(ctx, player).experience(), "experience shown in the summary");
            });
        }));

        cases.add(new GameTestCase(ID, "every_action", "Blocks, distance, mobs and jumps add up", 600, ctx -> {
            ctx.config().getExperience().experiencePerPoint = 0.1f;
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Busy", bed);
            mine(player.get(), 100);
            player.get().awardStat(Stats.WALK_ONE_CM, 50_000);
            player.get().awardStat(Stats.MOB_KILLS, 2);
            player.get().awardStat(Stats.JUMP, 100);
            sleepAlone(ctx, player, bed).then(() ->
                    ctx.equal(35, player.get().totalExperience, "experience for 350 points at 0.1"));
        }));

        cases.add(new GameTestCase(ID, "only_the_sleeper", "A player who stayed awake gets no experience", 600, ctx -> {
            ctx.sleepingPercentage(50);
            Bed bed = ctx.bed(10, 10);
            MockPlayer sleeper = ctx.joinAt("Sleeper", bed);
            MockPlayer awake = ctx.join("Awake");
            mine(sleeper.get(), 500);
            mine(awake.get(), 1000);
            sleepAlone(ctx, sleeper, bed).then(() -> {
                ctx.equal(45, sleeper.get().totalExperience, "experience of the sleeper");
                ctx.equal(0, awake.get().totalExperience, "experience of the awake player");
            });
        }));

        cases.add(new GameTestCase(ID, "each_their_own", "Three sleepers each get exactly their own experience", 600, ctx -> {
            Bed firstBed = ctx.bed(4, 10);
            Bed secondBed = ctx.bed(10, 10);
            Bed thirdBed = ctx.bed(16, 10);
            MockPlayer first = ctx.joinAt("First", firstBed);
            MockPlayer second = ctx.joinAt("Second", secondBed);
            MockPlayer third = ctx.joinAt("Third", thirdBed);
            mine(first.get(), 500);
            mine(second.get(), 100);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(first, firstBed);
                ctx.sleepOrFail(second, secondBed);
                ctx.sleepOrFail(third, thirdBed);
            }).sleepThroughTheNight("the night to pass", first).then(() -> {
                ctx.equal(45, first.get().totalExperience, "experience of the first player");
                ctx.equal(9, second.get().totalExperience, "experience of the second player");
                ctx.equal(0, third.get().totalExperience, "experience of the idle player");
                DailySummaryPacket summary = ctx.lastSummary(third);
                ctx.equal(45, ctx.summaryLine(summary, first).experience(), "first player's line");
                ctx.equal(9, ctx.summaryLine(summary, second).experience(), "second player's line");
                ctx.equal(0, ctx.summaryLine(summary, third).experience(), "idle player's line");
            });
        }));

        cases.add(new GameTestCase(ID, "got_up_early", "A player who got up before morning gets no experience", 600, ctx -> {
            ctx.sleepingPercentage(50);
            Bed firstBed = ctx.bed(4, 10);
            Bed secondBed = ctx.bed(16, 10);
            MockPlayer stayed = ctx.joinAt("Stayed", firstBed);
            MockPlayer left = ctx.joinAt("GotUp", secondBed);
            mine(stayed.get(), 100);
            mine(left.get(), 500);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(stayed, firstBed);
                ctx.sleepOrFail(left, secondBed);
            }).idle(20).then(() -> ctx.getUp(left))
                    .sleepThroughTheNight("the night to pass", stayed)
                    .then(() -> {
                        ctx.equal(9, stayed.get().totalExperience, "experience of the player who slept");
                        ctx.equal(0, left.get().totalExperience, "experience of the player who got up");
                    });
        }));

        cases.add(new GameTestCase(ID, "switched_off", "With experience switched off nobody gets any", 600, ctx -> {
            ctx.config().getExperience().enabled = false;
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Miner", bed);
            mine(player.get(), 500);
            sleepAlone(ctx, player, bed).then(() -> {
                ctx.equal(0, player.get().totalExperience, "experience with the option off");
                ctx.equal(0, line(ctx, player).experience(), "experience shown in the summary");
            });
        }));

        cases.add(new GameTestCase(ID, "configured_rate", "A changed rate is used", 600, ctx -> {
            ctx.config().getExperience().experiencePerPoint = 0.5f;
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Miner", bed);
            mine(player.get(), 10);
            sleepAlone(ctx, player, bed).then(() ->
                    ctx.equal(15, player.get().totalExperience, "experience for 30 points at 0.5"));
        }));

        cases.add(new GameTestCase(ID, "rounded_down", "Fractions of experience are rounded down", 600, ctx -> {
            Bed firstBed = ctx.bed(4, 10);
            Bed secondBed = ctx.bed(16, 10);
            MockPlayer below = ctx.joinAt("Below", firstBed);
            MockPlayer above = ctx.joinAt("Above", secondBed);
            mine(below.get(), 11);
            mine(above.get(), 12);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(below, firstBed);
                ctx.sleepOrFail(above, secondBed);
            }).sleepThroughTheNight("the night to pass", below).then(() -> {
                ctx.equal(0, below.get().totalExperience, "experience for 33 points");
                ctx.equal(1, above.get().totalExperience, "experience for 36 points");
            });
        }));

        cases.add(new GameTestCase(ID, "deaths_never_take_experience", "A bad day gives nothing and takes nothing", 600, ctx -> {
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Unlucky", bed);
            player.get().giveExperiencePoints(10);
            player.get().awardStat(Stats.MOB_KILLS, 1);
            player.get().awardStat(Stats.DEATHS, 1);
            sleepAlone(ctx, player, bed).then(() ->
                    ctx.equal(10, player.get().totalExperience, "experience after a day with more penalty than points"));
        }));

        cases.add(new GameTestCase(ID, "limit_per_sleep", "The limit caps the experience of one sleep", 600, ctx -> {
            ctx.config().getExperience().maxExperiencePerSleep = 30;
            Bed firstBed = ctx.bed(4, 10);
            Bed secondBed = ctx.bed(16, 10);
            MockPlayer busy = ctx.joinAt("Busy", firstBed);
            MockPlayer calm = ctx.joinAt("Calm", secondBed);
            mine(busy.get(), 5000);
            mine(calm.get(), 100);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(busy, firstBed);
                ctx.sleepOrFail(calm, secondBed);
            }).sleepThroughTheNight("the night to pass", busy).then(() -> {
                ctx.equal(30, busy.get().totalExperience, "experience capped by the limit");
                ctx.equal(9, calm.get().totalExperience, "experience below the limit");
                ctx.equal(30, ctx.summaryLine(ctx.lastSummary(busy), busy).experience(), "capped experience in the summary");
            });
        }));

        cases.add(new GameTestCase(ID, "second_night", "The second night counts only the new day", 1200, ctx -> {
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Regular", bed);
            mine(player.get(), 500);
            sleepAlone(ctx, player, bed).then(() -> {
                ctx.equal(45, player.get().totalExperience, "experience after the first night");
                mine(player.get(), 100);
                ctx.night();
            }).idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the second night to pass", player)
                    .then(() -> ctx.equal(54, player.get().totalExperience, "experience after the second night"));
        }));

        cases.add(new GameTestCase(ID, "without_mvp", "Experience does not depend on the MVP option", 600, ctx -> {
            ctx.config().getMvp().enabled = false;
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Miner", bed);
            mine(player.get(), 500);
            sleepAlone(ctx, player, bed).then(() ->
                    ctx.equal(45, player.get().totalExperience, "experience with MVP switched off"));
        }));

        cases.add(new GameTestCase(ID, "rewards_in_summary", "The summary carries the Well Rested level of every player", 600, ctx -> {
            Bed firstBed = ctx.bed(4, 10);
            Bed secondBed = SleepGameTests.litBed(ctx, 10);
            Bed thirdBed = ctx.bed(16, 10);
            MockPlayer winner = ctx.joinAt("Winner", firstBed);
            MockPlayer cosy = ctx.joinAt("Cosy", secondBed);
            MockPlayer plain = ctx.joinAt("Plain", thirdBed);
            mine(winner.get(), 500);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(winner, firstBed);
                ctx.sleepOrFail(cosy, secondBed);
                ctx.sleepOrFail(plain, thirdBed);
            }).sleepThroughTheNight("the night to pass", winner).then(() -> {
                DailySummaryPacket summary = ctx.lastSummary(plain);
                ctx.check(ctx.summaryLine(summary, winner).isMvp(), "the busiest player is not MVP");
                ctx.equal(5, ctx.summaryLine(summary, winner).wellRestedLevel(), "Well Rested level of the MVP");
                ctx.equal(1, ctx.summaryLine(summary, cosy).wellRestedLevel(), "Well Rested level in a lit bed");
                ctx.equal(0, ctx.summaryLine(summary, plain).wellRestedLevel(), "Well Rested level in a plain bed");
            });
        }));
    }

    private static TestContext sleepAlone(TestContext ctx, MockPlayer player, Bed bed) {
        ctx.night();
        return ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                .sleepThroughTheNight("the night to pass", player);
    }

    private static DailySummaryPacket.PlayerDailySummary line(TestContext ctx, MockPlayer player) {
        return ctx.summaryLine(ctx.lastSummary(player), player);
    }

    private static void mine(ServerPlayer player, int blocks) {
        player.awardStat(Stats.BLOCK_MINED.get(Blocks.STONE), blocks);
    }
}
