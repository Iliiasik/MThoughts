package mt.gametest;

import mt.gametest.TestContext.Bed;
import mt.network.packet.DailySummaryPacket;
import mt.server.WellRestedEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Blocks;

import java.util.List;
import java.util.function.BiConsumer;

final class MvpGameTests {

    private static final String ID = "mvp_score";

    private enum Winner { FIRST, SECOND, NOBODY }

    private record Day(BiConsumer<ServerPlayer, ServerPlayer> activity, Winner winner) {}

    private MvpGameTests() {}

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "MVP scoring", List.of(
                "mined blocks, distance, mob kills and jumps from the real player statistics add MVP points",
                "every action is worth exactly its configured points, checked against the score needed for MVP",
                "every death takes its configured penalty away and can cost a player the title",
                "changed point values in the config are used for the next night",
                "only players who slept compete, an awake player and a lone sleeper never get the title")));

        contest(cases, "blocks", "Each mined block is worth 3 points",
                new Day((first, second) -> mine(first, 3), Winner.NOBODY),
                new Day((first, second) -> mine(first, 4), Winner.FIRST));

        contest(cases, "distance", "Every 100 blocks travelled are worth 2 points",
                new Day((first, second) -> first.awardStat(Stats.WALK_ONE_CM, 40_000), Winner.NOBODY),
                new Day((first, second) -> first.awardStat(Stats.WALK_ONE_CM, 50_000), Winner.FIRST));

        contest(cases, "mobs", "Each mob kill is worth 15 points",
                new Day((first, second) -> {
                    first.awardStat(Stats.MOB_KILLS, 1);
                    mine(second, 5);
                }, Winner.NOBODY),
                new Day((first, second) -> {
                    first.awardStat(Stats.MOB_KILLS, 1);
                    mine(second, 4);
                }, Winner.FIRST));

        contest(cases, "jumps", "Every 10 jumps are worth 1 point",
                new Day((first, second) -> first.awardStat(Stats.JUMP, 99), Winner.NOBODY),
                new Day((first, second) -> first.awardStat(Stats.JUMP, 100), Winner.FIRST));

        contest(cases, "death_penalty", "Each death takes 30 points away",
                new Day((first, second) -> {
                    first.awardStat(Stats.MOB_KILLS, 1);
                    first.awardStat(Stats.DEATHS, 1);
                    mine(second, 4);
                }, Winner.SECOND),
                new Day((first, second) -> {
                    first.awardStat(Stats.MOB_KILLS, 3);
                    first.awardStat(Stats.DEATHS, 1);
                    mine(second, 5);
                }, Winner.NOBODY));

        cases.add(new GameTestCase(ID, "configured_points", "Changed point values are used", 600, ctx -> {
            ctx.config().getMvp().pointsPerBlock = 0;
            ctx.config().getMvp().pointsPerJump10 = 20;
            play(ctx, new Day((first, second) -> {
                mine(first, 10);
                second.awardStat(Stats.JUMP, 10);
            }, Winner.SECOND), null);
        }));
        cases.add(new GameTestCase(ID, "awake_players_ignored", "A player who stayed awake cannot take the title", 600, ctx -> {
            ctx.sleepingPercentage(50);
            Bed firstBed = ctx.bed(4, 10);
            Bed secondBed = ctx.bed(16, 10);
            MockPlayer first = ctx.joinAt("First", firstBed);
            MockPlayer second = ctx.joinAt("Second", secondBed);
            MockPlayer awake = ctx.join("Awake");
            mine(first.get(), 4);
            awake.get().awardStat(Stats.MOB_KILLS, 10);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(first, firstBed);
                ctx.sleepOrFail(second, secondBed);
            }).sleepThroughTheNight("the night to pass", first).then(() -> {
                SleepGameTests.expectSummary(ctx, first, 2);
                SleepGameTests.expectNoSummary(ctx, awake);
                expectWinner(ctx, "the night", Winner.FIRST, first, second);
                ctx.equal(0, ApiEventLog.of("mvp", awake.uuid()).size(), "MVP events for the awake player");
            });
        }));

        cases.add(new GameTestCase(ID, "lone_sleeper", "A lone sleeper is never MVP", 600, ctx -> {
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Alone", bed);
            player.get().awardStat(Stats.MOB_KILLS, 10);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the night to pass", player)
                    .then(() -> {
                        ctx.check(!ctx.summaryLine(ctx.lastSummary(player), player).isMvp(), "the lone sleeper was marked as MVP");
                        ctx.check(!WellRestedEffect.isMvp(player.get()), "the lone sleeper got the MVP effect");
                        ctx.equal(1, WellRestedEffect.getLevel(player.get()), "Well Rested level of the lone sleeper");
                    });
        }));
    }

    private static void contest(List<GameTestCase> cases, String name, String description, Day first, Day second) {
        cases.add(new GameTestCase(ID, name, description, 1200, ctx -> play(ctx, first, second)));
    }

    private static void play(TestContext ctx, Day firstDay, Day secondDay) {
        Bed firstBed = ctx.bed(4, 10);
        Bed secondBed = ctx.bed(16, 10);
        MockPlayer first = ctx.joinAt("First", firstBed);
        MockPlayer second = ctx.joinAt("Second", secondBed);
        night(ctx, "the first night", firstDay, first, firstBed, second, secondBed);
        if (secondDay != null) night(ctx, "the second night", secondDay, first, firstBed, second, secondBed);
    }

    private static void night(TestContext ctx, String what, Day day,
                              MockPlayer first, Bed firstBed, MockPlayer second, Bed secondBed) {
        ctx.then(() -> {
                    day.activity().accept(first.get(), second.get());
                    ctx.night();
                })
                .idle(2)
                .then(() -> {
                    ctx.sleepOrFail(first, firstBed);
                    ctx.sleepOrFail(second, secondBed);
                })
                .sleepThroughTheNight(what, first)
                .then(() -> expectWinner(ctx, what, day.winner(), first, second));
    }

    private static void expectWinner(TestContext ctx, String what, Winner winner, MockPlayer first, MockPlayer second) {
        DailySummaryPacket summary = ctx.lastSummary(first);
        ctx.equal(winner == Winner.FIRST, ctx.summaryLine(summary, first).isMvp(), "First is MVP after " + what);
        ctx.equal(winner == Winner.SECOND, ctx.summaryLine(summary, second).isMvp(), "Second is MVP after " + what);
    }

    private static void mine(ServerPlayer player, int blocks) {
        player.awardStat(Stats.BLOCK_MINED.get(Blocks.STONE), blocks);
    }
}
