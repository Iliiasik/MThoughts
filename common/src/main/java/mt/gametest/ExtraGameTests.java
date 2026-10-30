package mt.gametest;

import mt.gametest.TestContext.Bed;
import mt.gametest.TestContext.Decor;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.server.ComfortCalculator;
import mt.server.DailyStatsManager;
import mt.server.MTServer;
import mt.server.StatsStorage;
import mt.server.WellRestedEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("resource")
final class ExtraGameTests {

    private ExtraGameTests() {}

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        registerStats(categories, cases);
        registerPhantoms(categories, cases);
        registerApi(categories, cases);
        registerRestart(categories, cases);
        registerLoad(categories, cases);
        registerMoon(categories, cases);
    }

    private static void registerMoon(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        String id = "moon";
        categories.add(new TestCatalog.Category(id, "Moon phase", List.of(
                "the moon phase sent for the sleep overlay walks through all eight phases night after night",
                "a player in the nether still gets the phase of the overworld moon")));

        cases.add(new GameTestCase(id, "eight_nights", "Each night sends the phase of the overworld moon", 200, ctx -> {
            MockPlayer player = ctx.join("Stargazer");
            ctx.then(() -> {
                long firstDay = ctx.dayStart() / 24000L;
                List<Integer> phases = new ArrayList<>();
                for (int night = 0; night < 8; night++) {
                    ctx.level().setDayTime((firstDay + night) * 24000L + 18000L);
                    MTServer.sendMoonPhase(player.get());
                    int sent = ctx.lastSent(player, MoonPhasePacket.class).moonPhase();
                    ctx.equal((int) ((firstDay + night) % 8L), sent, "phase sent on night " + (night + 1));
                    phases.add(sent);
                }
                ctx.equal(8L, phases.stream().distinct().count(), "different phases over eight nights");
                player.moveTo(ctx.nether(), new BlockPos(0, 100, 0));
            }).idle(2).then(() -> {
                ctx.level().setDayTime(ctx.dayStart() + 24000L + 18000L);
                MTServer.sendMoonPhase(player.get());
                ctx.equal(ctx.level().getMoonPhase(), ctx.lastSent(player, MoonPhasePacket.class).moonPhase(),
                        "phase sent to a player in the nether");
            });
        }));
    }

    private static void registerStats(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        String id = "stats";
        categories.add(new TestCatalog.Category(id, "Stats since the last sleep", List.of(
                "blocks, distance, mobs, deaths, jumps and damage in the summary match what the player did",
                "a quiet day shows zeros and a broken saved baseline never makes a value negative")));

        cases.add(new GameTestCase(id, "exact_values", "Every stat in the summary matches what the player did", 600, ctx -> {
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Busy", bed);
            player.get().awardStat(Stats.BLOCK_MINED.get(Blocks.STONE), 7);
            player.get().awardStat(Stats.WALK_ONE_CM, 1500);
            player.get().awardStat(Stats.MOB_KILLS, 2);
            player.get().awardStat(Stats.DEATHS, 1);
            player.get().awardStat(Stats.JUMP, 12);
            player.get().awardStat(Stats.DAMAGE_DEALT, 30);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the night to pass", player)
                    .then(() -> {
                        DailySummaryPacket.PlayerDailySummary line = ctx.summaryLine(ctx.lastSummary(player), player);
                        ctx.equal(7, line.blocksDestroyed(), "blocks");
                        ctx.equal(1500, line.distanceWalked(), "distance");
                        ctx.equal(2, line.mobsKilled(), "mobs");
                        ctx.equal(1, line.deaths(), "deaths");
                        ctx.equal(12, line.jumps(), "jumps");
                        ctx.equal(30, line.damageDealt(), "damage");
                    });
        }));

        cases.add(new GameTestCase(id, "quiet_day_is_zero", "A day without activity shows zeros", 1200, ctx -> {
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Idle", bed);
            player.get().awardStat(Stats.JUMP, 9);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the first night", player)
                    .then(ctx::night)
                    .idle(2)
                    .then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the second night", player)
                    .then(() -> {
                        DailySummaryPacket.PlayerDailySummary line = ctx.summaryLine(ctx.lastSummary(player), player);
                        ctx.equal(0, line.jumps() + line.blocksDestroyed() + line.distanceWalked() + line.mobsKilled()
                                + line.deaths() + line.damageDealt(), "sum of all stats after a quiet day");
                    });
        }));

        cases.add(new GameTestCase(id, "never_negative", "A saved baseline above the current value never goes negative", 600, ctx -> {
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer[] player = {ctx.joinAt("Corrupted", bed)};
            player[0].get().awardStat(Stats.JUMP, 5);
            ctx.then(() -> {
                player[0].leave();
                StatsStorage.SavedPlayerStats saved = new StatsStorage.SavedPlayerStats();
                saved.baseJumps = 50;
                StatsStorage.savePlayerStats(ctx.server(), player[0].uuid(), saved);
                player[0] = ctx.rejoin(player[0], bed.standingSpot());
                ctx.night();
            }).idle(2).then(() -> ctx.sleepOrFail(player[0], bed))
                    .sleepThroughTheNight("the night to pass", player[0])
                    .then(() -> ctx.equal(0, ctx.summaryLine(ctx.lastSummary(player[0]), player[0]).jumps(), "jumps below the baseline"));
        }));
    }

    private static void registerPhantoms(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        String id = "phantoms";
        categories.add(new TestCatalog.Category(id, "Phantom timer", List.of(
                "when enabled, players who stayed awake have their phantom timer reset by a skipped night",
                "when disabled, their timer is left alone")));

        phantomCase(cases, id, true, "reset_when_enabled", "A skipped night resets the phantom timer of players who stayed awake");
        phantomCase(cases, id, false, "kept_when_disabled", "With the option off the phantom timer of awake players is kept");
    }

    private static void phantomCase(List<GameTestCase> cases, String id, boolean enabled, String name, String description) {
        cases.add(new GameTestCase(id, name, description, 600, ctx -> {
            ctx.config().getServer().resetPhantomTimerForNonSleepers = enabled;
            ctx.sleepingPercentage(50);
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer sleeper = ctx.joinAt("Sleeper", bed);
            MockPlayer awake = ctx.join("Insomniac");
            awake.get().awardStat(Stats.TIME_SINCE_REST, 50000);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(sleeper, bed))
                    .sleepThroughTheNight("the night to pass", sleeper)
                    .then(() -> {
                        int timer = awake.get().getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
                        if (enabled) {
                            ctx.check(timer < 1000, "the phantom timer was not reset: " + timer);
                        } else {
                            ctx.check(timer >= 50000, "the phantom timer was reset although the option is off: " + timer);
                        }
                    });
        }));
    }

    private static void registerApi(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        String id = "api";
        categories.add(new TestCatalog.Category(id, "Events for other mods", List.of(
                "comfort calculated, nightmare, Well Rested applied, Well Rested expired and MVP determined",
                "each fires exactly once with the right player and values on this loader")));

        cases.add(new GameTestCase(id, "comfort_and_nightmare", "Comfort and nightmare events fire once with the right values", 600, ctx -> {
            Bed bed = ctx.bed(10, 10);
            ctx.decorate(bed, Decor.MACABRE);
            MockPlayer player = ctx.joinAt("Watched", bed);
            ctx.night();
            ctx.idle(2).then(() -> {
                ComfortCalculator.invalidateCache(player.get());
                ctx.sleepOrFail(player, bed);
            }).idle(3).then(() -> {
                List<ApiEventLog.Entry> comfort = ApiEventLog.of("comfort", player.uuid());
                ctx.check(!comfort.isEmpty(), "no comfort event fired");
                ctx.equal(-1, comfort.getLast().value(), "comfort in the event");
                List<ApiEventLog.Entry> nightmares = ApiEventLog.of("nightmare", player.uuid());
                ctx.equal(1, nightmares.size(), "nightmare events");
                ctx.equal(-1, nightmares.getFirst().value(), "comfort in the nightmare event");
            });
        }));

        cases.add(new GameTestCase(id, "well_rested_events", "Applied and expired events carry level, MVP flag and duration", 100, ctx -> {
            MockPlayer player = ctx.join("Watched");
            ctx.then(() -> {
                WellRestedEffect.applyToPlayer(player.get(), 3);
                WellRestedEffect.clear(player.get());
                WellRestedEffect.applyMvpToPlayer(player.get());
                List<ApiEventLog.Entry> applied = ApiEventLog.of("applied", player.uuid());
                ctx.equal(2, applied.size(), "applied events");
                ctx.equal(List.of(3, false, 7 * 1200), List.of(applied.get(0).value(), applied.get(0).flag(), applied.get(0).extra()),
                        "first applied event");
                ctx.equal(List.of(5, true, 21 * 1200), List.of(applied.get(1).value(), applied.get(1).flag(), applied.get(1).extra()),
                        "MVP applied event");
                ctx.equal(1, ApiEventLog.of("expired", player.uuid()).size(), "expired events");
            });
        }));

        cases.add(new GameTestCase(id, "mvp_event", "The MVP event fires once for the best player", 600, ctx -> {
            Bed bestBed = ctx.bed(4, 10);
            Bed otherBed = ctx.bed(16, 10);
            MockPlayer best = ctx.joinAt("Best", bestBed);
            MockPlayer other = ctx.joinAt("Other", otherBed);
            best.get().awardStat(Stats.MOB_KILLS, 3);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(best, bestBed);
                ctx.sleepOrFail(other, otherBed);
            }).sleepThroughTheNight("the night to pass", best).then(() -> {
                ctx.equal(1, ApiEventLog.of("mvp", best.uuid()).size(), "MVP events for the best player");
                ctx.equal(0, ApiEventLog.of("mvp", other.uuid()).size(), "MVP events for the other player");
            });
        }));
    }

    private static void registerRestart(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        String id = "restart";
        categories.add(new TestCatalog.Category(id, "Server restart", List.of(
                "after a restart in the middle of the day no summary appears by itself",
                "stats since the last sleep are restored from disk and the next night continues from them")));

        cases.add(new GameTestCase(id, "restart_mid_day", "A restart mid day restores stats and gives no false summary", 1200, ctx -> {
            Bed bed = SleepGameTests.litBed(ctx, 10);
            MockPlayer[] player = {ctx.joinAt("Survivor", bed)};
            player[0].get().awardStat(Stats.JUMP, 20);
            int[] summariesBefore = new int[1];
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player[0], bed))
                    .sleepThroughTheNight("the first night", player[0])
                    .then(() -> {
                        player[0].get().awardStat(Stats.JUMP, 5);
                        player[0].leave();
                        DailyStatsManager.onServerStop(ctx.server());
                        player[0] = ctx.rejoin(player[0], bed.standingSpot());
                        ctx.setTime(3000L);
                    })
                    .idle(40)
                    .then(() -> {
                        summariesBefore[0] = ctx.summaries(player[0]).size();
                        ctx.equal(1, summariesBefore[0], "summaries received during the day after the restart");
                        ctx.night();
                    })
                    .idle(2)
                    .then(() -> ctx.sleepOrFail(player[0], bed))
                    .sleepThroughTheNight("the night after the restart", player[0])
                    .then(() -> ctx.equal(5, ctx.summaryLine(ctx.lastSummary(player[0]), player[0]).jumps(), "jumps after the restart"));
        }));
    }

    private static void registerLoad(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        String id = "load";
        categories.add(new TestCatalog.Category(id, "Load", List.of(
                "twenty players sleeping in the same night all get a complete summary in time")));

        cases.add(new GameTestCase(id, "twenty_players", "Twenty players sleep in one night and all get the full summary", 800, ctx -> {
            List<Bed> beds = new ArrayList<>();
            List<MockPlayer> players = new ArrayList<>();
            for (int row : new int[]{2, 12}) {
                for (int column = 1; column <= 19; column += 2) {
                    Bed bed = ctx.bed(column, row);
                    beds.add(bed);
                    players.add(ctx.joinAt("Guest" + players.size(), bed));
                }
            }
            ctx.night();
            ctx.idle(2).then(() -> {
                for (int i = 0; i < players.size(); i++) {
                    ctx.sleepOrFail(players.get(i), beds.get(i));
                }
            }).sleepThroughTheNight("the night to pass", players.getFirst()).then(() -> {
                for (MockPlayer player : players) {
                    SleepGameTests.expectSummary(ctx, player, players.size());
                }
            });
        }));
    }
}
