package mt.gametest;

import mt.gametest.TestContext.Bed;
import mt.network.packet.SleepingPlayersPacket;
import net.minecraft.world.level.GameType;

import java.util.List;

final class SleepingHudGameTests {

    private static final String ID = "sleeping_hud";

    private SleepingHudGameTests() {}

    static void expectCounts(TestContext ctx, MockPlayer viewer, int sleeping, int total) {
        SleepingPlayersPacket counts = ctx.lastSent(viewer, SleepingPlayersPacket.class);
        ctx.equal(sleeping + " / " + total, counts.sleepingCount() + " / " + counts.totalPlayers(),
                "sleeping players shown to " + viewer.name());
    }

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "Sleeping players HUD", List.of(
                "the sleeping and total counters follow players lying down, getting up, joining and leaving",
                "spectators and players outside the overworld are not counted, like in vanilla",
                "the counters are only sent when they change",
                "the vanilla sleep message in the action bar is hidden or shown as configured")));

        cases.add(new GameTestCase(ID, "follows_beds", "The counters follow players lying down and getting up", 200, ctx -> {
            Bed first = ctx.bed(4, 10);
            Bed second = ctx.bed(16, 10);
            MockPlayer a = ctx.joinAt("Alpha", first);
            MockPlayer b = ctx.joinAt("Beta", second);
            MockPlayer c = ctx.join("Gamma");
            ctx.night();
            ctx.idle(2).then(() -> {
                expectCounts(ctx, c, 0, 3);
                ctx.sleepOrFail(a, first);
            }).idle(2).then(() -> {
                expectCounts(ctx, c, 1, 3);
                ctx.sleepOrFail(b, second);
            }).idle(2).then(() -> {
                expectCounts(ctx, c, 2, 3);
                ctx.getUp(a);
            }).idle(2).then(() -> expectCounts(ctx, c, 1, 3));
        }));

        cases.add(new GameTestCase(ID, "follows_joins_and_leaves", "The counters follow players joining and leaving", 200, ctx -> {
            Bed bed = ctx.bed(16, 10);
            MockPlayer viewer = ctx.join("Viewer");
            MockPlayer sleeper = ctx.joinAt("Sleeper", bed);
            MockPlayer[] guest = new MockPlayer[1];
            ctx.night();
            ctx.idle(2).then(() -> {
                expectCounts(ctx, viewer, 0, 2);
                guest[0] = ctx.join("Guest");
            }).idle(2).then(() -> {
                expectCounts(ctx, viewer, 0, 3);
                guest[0].leave();
            }).idle(2).then(() -> {
                expectCounts(ctx, viewer, 0, 2);
                ctx.sleepOrFail(sleeper, bed);
            }).idle(2).then(() -> {
                expectCounts(ctx, viewer, 1, 2);
                sleeper.leave();
            }).idle(2).then(() -> expectCounts(ctx, viewer, 0, 1));
        }));

        cases.add(new GameTestCase(ID, "spectators_not_counted", "Spectators are not counted", 100, ctx -> {
            MockPlayer player = ctx.join("Player");
            ctx.joinAs("Watcher", GameType.SPECTATOR);
            ctx.idle(2).then(() -> expectCounts(ctx, player, 0, 1));
        }));

        cases.add(new GameTestCase(ID, "only_the_overworld", "Only players in the overworld are counted", 200, ctx -> {
            MockPlayer home = ctx.join("Home");
            MockPlayer traveller = ctx.join("Traveller");
            ctx.idle(2).then(() -> {
                expectCounts(ctx, home, 0, 2);
                traveller.moveTo(ctx.nether(), new net.minecraft.core.BlockPos(0, 100, 0));
            }).idle(2).then(() -> {
                expectCounts(ctx, home, 0, 1);
                traveller.moveTo(ctx.level(), ctx.at(10, 1, 6));
            }).idle(2).then(() -> expectCounts(ctx, home, 0, 2));
        }));

        cases.add(new GameTestCase(ID, "sent_only_on_change", "The counters are only sent when they change", 200, ctx -> {
            MockPlayer player = ctx.join("Quiet");
            int[] before = new int[1];
            ctx.idle(5).then(() -> before[0] = ctx.sent(player, SleepingPlayersPacket.class).size())
                    .idle(40)
                    .then(() -> ctx.equal(before[0], ctx.sent(player, SleepingPlayersPacket.class).size(),
                            "counter packets sent while nothing changed"));
        }));

        cases.add(new GameTestCase(ID, "vanilla_message_hidden_or_shown", "The vanilla sleep message is hidden or shown as configured", 200, ctx -> {
            Bed bed = ctx.bed(10, 10);
            MockPlayer sleeper = ctx.joinAt("Sleeper", bed);
            MockPlayer witness = ctx.join("Witness");
            ctx.night();
            ctx.idle(2).then(() -> {
                witness.forgetActionBar();
                ctx.sleepOrFail(sleeper, bed);
            }).idle(2).then(() -> {
                ctx.check(!witness.sawActionBar("sleep."), "the vanilla sleep message was shown while hidden");
                ctx.config().getServer().suppressVanillaSleepMessages = false;
                ctx.getUp(sleeper);
                witness.forgetActionBar();
                ctx.sleepOrFail(sleeper, bed);
            }).idle(2).then(() -> ctx.check(witness.sawActionBar("sleep.players_sleeping"),
                    "the vanilla sleep message was not shown while allowed"));
        }));
    }
}
