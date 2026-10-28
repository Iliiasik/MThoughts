package mt.gametest;

import mt.gametest.TestContext.Bed;
import mt.gametest.TestContext.Decor;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.WellRestedPacket;
import mt.server.WellRestedEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;

import java.util.List;

@SuppressWarnings("resource")
final class SleepGameTests {

    private static final String ID = "sleep";

    private SleepGameTests() {}

    static Bed litBed(TestContext ctx, int x) {
        Bed bed = ctx.bed(x, 10);
        ctx.decorate(bed, Decor.LIGHTING);
        return bed;
    }

    static void expectSummary(TestContext ctx, MockPlayer player, int lines) {
        ctx.check(!ctx.summaries(player).isEmpty(), player.name() + " got no summary");
        DailySummaryPacket summary = ctx.lastSummary(player);
        ctx.equal(lines, summary.summaries().size(), "players listed in the summary of " + player.name());
        ctx.summaryLine(summary, player);
    }

    static void expectNoSummary(TestContext ctx, MockPlayer player) {
        ctx.check(ctx.summaries(player).isEmpty(), player.name() + " got a summary but should not");
    }

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "Sleep and the daily summary", List.of(
                "a full night, getting up and lying down again, one or both of two players sleeping",
                "whoever gets up before the night passes gets no summary and no effect",
                "/time set day, lying until morning without a skip and a sleeping percentage above 100",
                "thunderstorms: a skip, getting up during the storm and staying in bed until it ends",
                "gradual time skips like sleep mods do, and worlds with the daylight cycle switched off",
                "leaving after sleeping, joining at night, nether beds and daytime attempts change nothing",
                "several nights in a row reset the stats only for sleepers, the nights counter matches the game",
                "a nightmare bed still gives a summary and shows the nightmare during the night")));

        cases.add(new GameTestCase(ID, "full_night", "One player sleeps through the night and gets a summary", 600, ctx -> {
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Sleeper", bed);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the night to pass", player)
                    .then(() -> {
                        expectSummary(ctx, player, 1);
                        ctx.equal(1, WellRestedEffect.getLevel(player.get()), "Well Rested level");
                    });
        }));

        cases.add(new GameTestCase(ID, "up_and_back_to_bed", "Getting up and lying down again still gives a summary", 600, ctx -> {
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Restless", bed);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .idle(20).then(() -> ctx.getUp(player))
                    .idle(5).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the night to pass", player)
                    .then(() -> expectSummary(ctx, player, 1));
        }));

        cases.add(new GameTestCase(ID, "both_players_sleep", "Two players sleep and both get the summary", 600, ctx -> {
            Bed first = litBed(ctx, 4);
            Bed second = litBed(ctx, 16);
            MockPlayer a = ctx.joinAt("Alpha", first);
            MockPlayer b = ctx.joinAt("Beta", second);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(a, first);
                ctx.sleepOrFail(b, second);
            }).sleepThroughTheNight("the night to pass", a).then(() -> {
                expectSummary(ctx, a, 2);
                expectSummary(ctx, b, 2);
            });
        }));

        cases.add(new GameTestCase(ID, "one_of_two_sleeps", "Only the player who slept gets the summary and the effect", 600, ctx -> {
            ctx.sleepingPercentage(50);
            Bed bed = litBed(ctx, 10);
            MockPlayer sleeper = ctx.joinAt("Sleeper", bed);
            MockPlayer awake = ctx.join("Awake");
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(sleeper, bed))
                    .sleepThroughTheNight("the night to pass", sleeper)
                    .then(() -> {
                        expectSummary(ctx, sleeper, 1);
                        expectNoSummary(ctx, awake);
                        ctx.check(WellRestedEffect.hasEffect(sleeper.get()), "the sleeper got no effect");
                        ctx.check(!WellRestedEffect.hasEffect(awake.get()), "the awake player got an effect");
                    });
        }));

        cases.add(new GameTestCase(ID, "one_gets_up_before_the_skip", "A player who got up before the skip gets nothing", 600, ctx -> {
            ctx.sleepingPercentage(50);
            Bed first = litBed(ctx, 4);
            Bed second = litBed(ctx, 16);
            MockPlayer stayed = ctx.joinAt("Stayed", first);
            MockPlayer left = ctx.joinAt("GotUp", second);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(stayed, first);
                ctx.sleepOrFail(left, second);
            }).idle(20).then(() -> ctx.getUp(left))
                    .sleepThroughTheNight("the night to pass", stayed)
                    .then(() -> {
                        expectSummary(ctx, stayed, 1);
                        expectNoSummary(ctx, left);
                        ctx.check(!WellRestedEffect.hasEffect(left.get()), "the player who got up got an effect");
                    });
        }));

        cases.add(new GameTestCase(ID, "got_up_and_came_back", "A player who got up and lay down again gets the summary", 700, ctx -> {
            Bed first = litBed(ctx, 4);
            Bed second = litBed(ctx, 16);
            MockPlayer stayed = ctx.joinAt("Stayed", first);
            MockPlayer back = ctx.joinAt("CameBack", second);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(stayed, first);
                ctx.sleepOrFail(back, second);
            }).idle(20).then(() -> ctx.getUp(back))
                    .idle(20).then(() -> ctx.sleepOrFail(back, second))
                    .sleepThroughTheNight("the night to pass", stayed)
                    .then(() -> {
                        expectSummary(ctx, stayed, 2);
                        expectSummary(ctx, back, 2);
                    });
        }));

        cases.add(new GameTestCase(ID, "time_set_day", "/time set day while sleeping gives a summary", 500, ctx -> {
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Sleeper", bed);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .idle(20).then(() -> ctx.check(ctx.op("time set day").result() > 0, "/time set day failed"))
                    .waitFor("the player to wake up", 40, () -> !player.get().isSleeping())
                    .idle(3)
                    .then(() -> expectSummary(ctx, player, 1));
        }));

        cases.add(new GameTestCase(ID, "lying_until_morning", "Lying in bed until morning without a skip gives a summary", 700, ctx -> {
            Bed bed = litBed(ctx, 10);
            MockPlayer sleeper = ctx.joinAt("EarlyBird", bed);
            MockPlayer awake = ctx.join("NightOwl");
            ctx.setTime(23200L);
            ctx.idle(2).then(() -> ctx.sleepOrFail(sleeper, bed))
                    .waitFor("the morning", 400, () -> ctx.level().isDay() && !sleeper.get().isSleeping())
                    .idle(3)
                    .then(() -> {
                        expectSummary(ctx, sleeper, 1);
                        expectNoSummary(ctx, awake);
                    });
        }));

        cases.add(new GameTestCase(ID, "sleeping_percentage_above_100", "With a sleeping percentage above 100 the morning still gives a summary", 700, ctx -> {
            ctx.sleepingPercentage(101);
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Alone", bed);
            ctx.setTime(23200L);
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .waitFor("the morning", 400, () -> ctx.level().isDay() && !player.get().isSleeping())
                    .idle(3)
                    .then(() -> expectSummary(ctx, player, 1));
        }));

        cases.add(new GameTestCase(ID, "thunderstorm_skip", "Sleeping through a daytime thunderstorm gives a summary", 700, ctx -> {
            ctx.weatherCycle(true);
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("StormSleeper", bed);
            ctx.noonThunderstorm();
            ctx.idle(3).then(() -> ctx.sleepOrFail(player, bed))
                    .waitFor("the summary after the storm", 400, () -> !ctx.summaries(player).isEmpty())
                    .then(() -> expectSummary(ctx, player, 1));
        }));

        cases.add(new GameTestCase(ID, "got_up_during_the_storm", "Getting up during a storm and the storm ending gives nothing", 500, ctx -> {
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Restless", bed);
            ctx.join("Witness");
            ctx.noonThunderstorm();
            ctx.idle(3).then(() -> ctx.sleepOrFail(player, bed))
                    .idle(20).then(() -> ctx.getUp(player))
                    .idle(20).then(ctx::clearWeather)
                    .waitFor("the storm to clear", 40, () -> ctx.level().isDay())
                    .idle(5)
                    .then(() -> expectNoSummary(ctx, player));
        }));

        cases.add(new GameTestCase(ID, "in_bed_until_the_storm_ends", "Staying in bed until a storm ends gives a summary", 500, ctx -> {
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Patient", bed);
            ctx.join("Witness");
            ctx.noonThunderstorm();
            ctx.idle(3).then(() -> ctx.sleepOrFail(player, bed))
                    .idle(30).then(ctx::clearWeather)
                    .waitFor("the summary", 100, () -> !ctx.summaries(player).isEmpty())
                    .then(() -> expectSummary(ctx, player, 1));
        }));

        cases.add(new GameTestCase(ID, "gradual_skip_like_sleep_mods", "Speeding time up like sleep mods do still gives a summary", 600, ctx -> {
            ctx.sleepingPercentage(101);
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Modded", bed);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .waitFor("the morning", 200, () -> {
                        ServerLevel level = ctx.level();
                        if (!level.isDay()) level.setDayTime(level.getDayTime() + 300L);
                        return level.isDay() && !player.get().isSleeping();
                    })
                    .idle(3)
                    .then(() -> expectSummary(ctx, player, 1));
        }));

        cases.add(new GameTestCase(ID, "daylight_cycle_off", "With the daylight cycle off a skip is not counted as getting up", 600, ctx -> {
            ctx.daylightCycle(false);
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Timeless", bed);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .waitFor("the skip to wake the player", 300, () -> !player.get().isSleeping())
                    .idle(3)
                    .then(() -> {
                        ctx.check(!ctx.level().isDay(), "the time moved although the daylight cycle is off");
                        expectNoSummary(ctx, player);
                        ctx.check(ctx.op("time set day").result() > 0, "/time set day failed");
                    })
                    .idle(3)
                    .then(() -> expectSummary(ctx, player, 1));
        }));

        cases.add(new GameTestCase(ID, "left_after_sleeping", "Leaving after sleeping breaks nothing for the others", 600, ctx -> {
            ctx.sleepingPercentage(50);
            Bed first = litBed(ctx, 4);
            Bed second = litBed(ctx, 16);
            MockPlayer leaver = ctx.joinAt("Leaver", first);
            MockPlayer stayer = ctx.joinAt("Stayer", second);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(leaver, first);
                ctx.sleepOrFail(stayer, second);
            }).idle(30).then(leaver::leave)
                    .sleepThroughTheNight("the night to pass", stayer)
                    .then(() -> {
                        expectSummary(ctx, stayer, 1);
                        expectNoSummary(ctx, leaver);
                    });
        }));

        cases.add(new GameTestCase(ID, "joined_at_night", "A player who joins at night and does not sleep gets nothing", 600, ctx -> {
            ctx.sleepingPercentage(50);
            Bed bed = litBed(ctx, 10);
            MockPlayer sleeper = ctx.joinAt("Sleeper", bed);
            MockPlayer[] late = new MockPlayer[1];
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(sleeper, bed))
                    .idle(10).then(() -> late[0] = ctx.join("Latecomer"))
                    .sleepThroughTheNight("the night to pass", sleeper)
                    .then(() -> {
                        expectSummary(ctx, sleeper, 1);
                        expectNoSummary(ctx, late[0]);
                    });
        }));

        cases.add(new GameTestCase(ID, "nether_bed", "A bed in the nether counts nothing", 500, ctx -> {
            MockPlayer player = ctx.join("Explorer");
            ServerLevel nether = ctx.nether();
            BlockPos foot = new BlockPos(0, 100, 0);
            BlockPos head = foot.relative(Direction.SOUTH);
            ctx.then(() -> {
                BlockState base = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH);
                nether.setBlock(head, base.setValue(BedBlock.PART, BedPart.HEAD), Block.UPDATE_ALL);
                nether.setBlock(foot, base.setValue(BedBlock.PART, BedPart.FOOT), Block.UPDATE_ALL);
                player.moveTo(nether, foot.east());
                ctx.night();
            }).idle(2).then(() -> {
                var result = player.get().startSleepInBed(head);
                ctx.equal(Player.BedSleepingProblem.NOT_POSSIBLE_HERE, result.left().orElse(null), "nether bed answer");
                ctx.check(!player.get().isSleeping(), "the player fell asleep in the nether");
            }).idle(3).then(ctx::day).idle(3).then(() -> expectNoSummary(ctx, player));
        }));

        cases.add(new GameTestCase(ID, "no_sleep_during_the_day", "Trying to sleep during the day counts nothing", 200, ctx -> {
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Napper", bed);
            ctx.day();
            ctx.idle(2).then(() -> {
                ctx.equal(Player.BedSleepingProblem.NOT_POSSIBLE_NOW, ctx.sleep(player, bed).left().orElse(null), "daytime answer");
                ctx.night();
            }).idle(3).then(ctx::day).idle(3).then(() -> expectNoSummary(ctx, player));
        }));

        cases.add(new GameTestCase(ID, "several_nights", "Several nights reset the stats only for those who slept", 1200, ctx -> {
            ctx.sleepingPercentage(50);
            Bed first = litBed(ctx, 4);
            Bed second = litBed(ctx, 16);
            MockPlayer a = ctx.joinAt("Regular", first);
            MockPlayer b = ctx.joinAt("Skipper", second);
            a.get().awardStat(Stats.JUMP, 20);
            b.get().awardStat(Stats.JUMP, 20);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(a, first))
                    .sleepThroughTheNight("the first night", a)
                    .then(() -> {
                        ctx.equal(20, ctx.summaryLine(ctx.lastSummary(a), a).jumps(), "jumps on the first night");
                        a.get().awardStat(Stats.JUMP, 10);
                        b.get().awardStat(Stats.JUMP, 10);
                        ctx.sleepingPercentage(100);
                        ctx.night();
                    })
                    .idle(2)
                    .then(() -> {
                        ctx.sleepOrFail(a, first);
                        ctx.sleepOrFail(b, second);
                    })
                    .sleepThroughTheNight("the second night", a)
                    .then(() -> {
                        DailySummaryPacket summary = ctx.lastSummary(b);
                        ctx.equal(10, ctx.summaryLine(summary, a).jumps(), "jumps since the first night for the sleeper");
                        ctx.equal(30, ctx.summaryLine(summary, b).jumps(), "jumps kept by the player who skipped a night");
                    });
        }));

        cases.add(new GameTestCase(ID, "nights_counter", "The nights counter matches the game statistic", 1200, ctx -> {
            Bed bed = litBed(ctx, 10);
            MockPlayer player = ctx.joinAt("Counter", bed);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the first night", player)
                    .then(() -> {
                        ctx.equal(1, ctx.summaryLine(ctx.lastSummary(player), player).nightsSlept(), "nights after one night");
                        ctx.night();
                    })
                    .idle(2)
                    .then(() -> ctx.sleepOrFail(player, bed))
                    .sleepThroughTheNight("the second night", player)
                    .then(() -> {
                        int counted = player.get().getStats().getValue(Stats.CUSTOM.get(Stats.SLEEP_IN_BED));
                        ctx.equal(2, counted, "the game statistic");
                        ctx.equal(counted, ctx.summaryLine(ctx.lastSummary(player), player).nightsSlept(), "nights after two nights");
                    });
        }));

        cases.add(new GameTestCase(ID, "nightmare_bed", "A nightmare bed shows the nightmare and still gives a summary", 600, ctx -> {
            Bed bed = ctx.bed(10, 10);
            ctx.decorate(bed, Decor.MACABRE);
            MockPlayer player = ctx.joinAt("Haunted", bed);
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(player, bed))
                    .idle(3)
                    .then(() -> ctx.check(ctx.lastSent(player, WellRestedPacket.class).nightmareMode(), "the nightmare was not shown"))
                    .sleepThroughTheNight("the night to pass", player)
                    .then(() -> {
                        expectSummary(ctx, player, 1);
                        ctx.check(!WellRestedEffect.hasEffect(player.get()), "a nightmare gave a Well Rested effect");
                        List<ApiEventLog.Entry> nightmares = ApiEventLog.of("nightmare", player.uuid());
                        ctx.equal(1, nightmares.size(), "nightmare events fired");
                        ctx.equal(-1, nightmares.getFirst().value(), "comfort sent with the nightmare event");
                    });
        }));
    }
}
