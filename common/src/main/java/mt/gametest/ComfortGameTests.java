package mt.gametest;

import mt.config.MidnightThoughtsConfig;
import mt.gametest.TestContext.Bed;
import mt.gametest.TestContext.Decor;
import mt.network.packet.WellRestedPacket;
import mt.server.ComfortCalculator;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

final class ComfortGameTests {

    private static final String ID = "comfort";

    private ComfortGameTests() {}

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "Comfort", List.of(
                "comfort is measured around the bed the player lies in, never around the player",
                "the scan radius from the config is honoured, including 0 and the cap of 8",
                "every tag uses its own configured weight and counts once however many blocks it has",
                "cosy and unsettling blocks add up, switching beds always uses the new bed",
                "a blocked bed refuses sleep, the cached value lives 5 seconds and is dropped on leave or reload",
                "the comfort, nightmare and sleep block switches and thresholds work on their own",
                "the comfort event from the API can change the final value")));

        cases.add(new GameTestCase(ID, "measured_around_the_bed", "Comfort is measured around the bed, not the player", 200, ctx -> {
            ctx.config().getComfort().scanRadius = 2;
            Bed bed = ctx.bed(10, 10);
            ctx.place(bed.head().offset(-1, 0, 1), Blocks.GLOWSTONE);
            MockPlayer player = ctx.joinAt("Sleeper", bed);
            ctx.place(ctx.at(13, 1, 10), Blocks.SKELETON_SKULL);
            ctx.night();
            ctx.then(() -> {
                ctx.equal(1, ctx.comfortAt(player, bed), "comfort around the bed");
                ctx.sleepOrFail(player, bed);
            }).idle(2).then(() -> ctx.equal(1, ctx.sessionComfort(player), "comfort kept for the night"));
        }));

        cases.add(new GameTestCase(ID, "radius_edge", "A block on the radius edge counts, one step further does not", 100, ctx -> {
            ctx.config().getComfort().scanRadius = 3;
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Measurer", bed);
            ctx.then(() -> {
                ctx.place(bed.head().offset(-3, 0, 0), Blocks.GLOWSTONE);
                ctx.equal(1, ctx.comfortAt(player, bed), "comfort with a lamp 3 blocks away");
                ctx.clear(bed.head().offset(-3, 0, 0));
                ctx.place(bed.head().offset(-4, 0, 0), Blocks.GLOWSTONE);
                ctx.equal(0, ctx.comfortAt(player, bed), "comfort with a lamp 4 blocks away");
            });
        }));

        cases.add(new GameTestCase(ID, "radius_zero", "Radius 0 looks only at the bed itself", 100, ctx -> {
            ctx.config().getComfort().scanRadius = 0;
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Measurer", bed);
            ctx.place(bed.head().offset(-1, 0, 0), Blocks.GLOWSTONE);
            ctx.then(() -> ctx.equal(0, ctx.comfortAt(player, bed), "comfort with radius 0"));
        }));

        cases.add(new GameTestCase(ID, "radius_capped_at_eight", "The radius is capped at 8 blocks", 100, ctx -> {
            ctx.config().getComfort().scanRadius = 20;
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Measurer", bed);
            ctx.then(() -> {
                ctx.place(bed.head().offset(8, 0, 0), Blocks.GLOWSTONE);
                ctx.equal(1, ctx.comfortAt(player, bed), "comfort with a lamp 8 blocks away");
                ctx.clear(bed.head().offset(8, 0, 0));
                ctx.place(bed.head().offset(9, 0, 0), Blocks.GLOWSTONE);
                ctx.equal(0, ctx.comfortAt(player, bed), "comfort with a lamp 9 blocks away");
            });
        }));

        cases.add(new GameTestCase(ID, "weights_per_tag", "Every tag uses its own configured weight", 100, ctx -> {
            MidnightThoughtsConfig.ComfortWeights weights = ctx.config().getComfort().weights;
            weights.lighting = 2;
            weights.carpet = 3;
            weights.furniture = 4;
            weights.decoration = 5;
            weights.structure = 6;
            weights.macabre = -2;
            weights.hostile = -3;
            weights.dark = -4;
            int[] expected = {2, 3, 4, 5, 6, -2, -3, -4};
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Measurer", bed);
            ctx.then(() -> {
                Decor[] decor = Decor.values();
                for (int i = 0; i < decor.length; i++) {
                    ctx.undecorate(bed);
                    ctx.decorate(bed, 0, decor[i]);
                    ctx.equal(expected[i], ctx.comfortAt(player, bed), "comfort with only " + decor[i]);
                }
            });
        }));

        cases.add(new GameTestCase(ID, "a_tag_counts_once", "A tag counts once however many blocks it has", 100, ctx -> {
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Measurer", bed);
            ctx.decorate(bed, Decor.LIGHTING, Decor.LIGHTING, Decor.LIGHTING, Decor.LIGHTING, Decor.LIGHTING);
            ctx.then(() -> ctx.equal(1, ctx.comfortAt(player, bed), "comfort with five lamps"));
        }));

        cases.add(new GameTestCase(ID, "cosy_and_unsettling_add_up", "Cosy and unsettling blocks add up", 100, ctx -> {
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Measurer", bed);
            ctx.then(() -> {
                ctx.decorate(bed, Decor.LIGHTING, Decor.CARPET, Decor.FURNITURE, Decor.MACABRE, Decor.HOSTILE);
                ctx.equal(1, ctx.comfortAt(player, bed), "three cosy and two unsettling tags");
                ctx.undecorate(bed);
                ctx.decorate(bed, Decor.values());
                ctx.equal(2, ctx.comfortAt(player, bed), "all eight tags");
            });
        }));

        cases.add(new GameTestCase(ID, "switch_to_a_nightmare_bed", "Getting up and lying in a nightmare bed uses the new bed", 200, ctx -> {
            Bed cosy = ctx.bed(4, 10);
            Bed scary = ctx.bed(16, 10);
            ctx.decorate(cosy, Decor.LIGHTING, Decor.CARPET, Decor.FURNITURE, Decor.DECORATION, Decor.STRUCTURE);
            ctx.decorate(scary, Decor.MACABRE);
            MockPlayer player = ctx.joinAt("Restless", cosy);
            ctx.night();
            ctx.then(() -> ctx.sleepOrFail(player, cosy))
                    .idle(2)
                    .then(() -> {
                        ctx.equal(5, ctx.sessionComfort(player), "comfort in the cosy bed");
                        ctx.check(!ctx.lastSent(player, WellRestedPacket.class).nightmareMode(), "the cosy bed must not show a nightmare");
                        ctx.getUp(player);
                        ctx.sleepOrFail(player, scary);
                    })
                    .idle(2)
                    .then(() -> {
                        ctx.equal(-1, ctx.sessionComfort(player), "comfort in the nightmare bed");
                        ctx.check(ctx.lastSent(player, WellRestedPacket.class).nightmareMode(), "the nightmare bed must show a nightmare");
                    });
        }));

        cases.add(new GameTestCase(ID, "switch_to_a_blocked_bed", "Getting up and trying a blocked bed refuses sleep", 200, ctx -> {
            Bed cosy = ctx.bed(4, 10);
            Bed blocked = ctx.bed(16, 10);
            ctx.decorate(cosy, Decor.LIGHTING, Decor.CARPET);
            ctx.decorate(blocked, Decor.MACABRE, Decor.HOSTILE);
            MockPlayer player = ctx.joinAt("Restless", cosy);
            ctx.night();
            ctx.then(() -> ctx.sleepOrFail(player, cosy))
                    .idle(2)
                    .then(() -> {
                        ctx.getUp(player);
                        player.forgetActionBar();
                        ctx.check(ctx.sleep(player, blocked).left().isPresent(), "the blocked bed accepted the player");
                    })
                    .idle(1)
                    .then(() -> {
                        ctx.check(!player.get().isSleeping(), "the player is asleep in a blocked bed");
                        ctx.check(player.sawActionBar("midnightthoughts.sleep.nightmare_blocked"), "the player was not told why");
                    });
        }));

        cases.add(new GameTestCase(ID, "cache_lives_five_seconds", "A cached blocked bed stays blocked for 5 seconds", 300, ctx -> {
            Bed bed = ctx.bed(10, 10);
            ctx.decorate(bed, Decor.MACABRE, Decor.HOSTILE);
            MockPlayer player = ctx.joinAt("Impatient", bed);
            ctx.night();
            ctx.then(() -> {
                ComfortCalculator.invalidateCache(player.get());
                ctx.check(ctx.sleep(player, bed).left().isPresent(), "the blocked bed accepted the player");
                ctx.undecorate(bed);
            }).idle(20).then(() -> ctx.check(ctx.sleep(player, bed).left().isPresent(),
                    "the cached answer was dropped before 5 seconds"))
                    .idle(90)
                    .then(() -> ctx.check(ctx.sleep(player, bed).right().isPresent(),
                            "the bed is still blocked after the cache expired"));
        }));

        cases.add(new GameTestCase(ID, "cache_dropped_on_leave", "Leaving the game drops the cached comfort", 100, ctx -> {
            Bed bed = ctx.bed(10, 10);
            ctx.decorate(bed, Decor.MACABRE);
            MockPlayer[] player = {ctx.joinAt("Leaver", bed)};
            ctx.then(() -> {
                ComfortCalculator.invalidateCache(player[0].get());
                ctx.equal(-1, ComfortCalculator.calculateComfortLevel(player[0].get(), bed.head()), "comfort before leaving");
                ctx.undecorate(bed);
                player[0] = ctx.rejoin(player[0], bed.standingSpot());
                ctx.equal(0, ComfortCalculator.calculateComfortLevel(player[0].get(), bed.head()), "comfort after coming back");
            });
        }));

        cases.add(new GameTestCase(ID, "cache_dropped_on_reload", "Reloading the config drops the cached comfort", 100, ctx -> {
            Bed bed = ctx.bed(10, 10);
            ctx.decorate(bed, Decor.MACABRE);
            MockPlayer player = ctx.joinAt("Admin", bed);
            ctx.then(() -> {
                ComfortCalculator.invalidateCache(player.get());
                ctx.equal(-1, ComfortCalculator.calculateComfortLevel(player.get(), bed.head()), "comfort before the reload");
                ctx.undecorate(bed);
                ctx.equal(-1, ComfortCalculator.calculateComfortLevel(player.get(), bed.head()), "comfort is not cached");
                ctx.check(ctx.op("midnightthoughts reload config").result() > 0, "the reload command failed");
                ctx.equal(0, ComfortCalculator.calculateComfortLevel(player.get(), bed.head()), "comfort after the reload");
            });
        }));

        cases.add(new GameTestCase(ID, "comfort_switched_off", "Switching comfort off gives 0 and never blocks a bed", 100, ctx -> {
            ctx.config().getComfort().enabled = false;
            Bed bed = ctx.bed(10, 10);
            ctx.decorate(bed, Decor.MACABRE, Decor.HOSTILE, Decor.DARK);
            MockPlayer player = ctx.joinAt("Unbothered", bed);
            ctx.night();
            ctx.then(() -> {
                ctx.equal(0, ctx.comfortAt(player, bed), "comfort while switched off");
                ctx.sleepOrFail(player, bed);
            });
        }));

        cases.add(new GameTestCase(ID, "switches_work_alone", "Nightmares and sleep blocking switch off on their own", 200, ctx -> {
            Bed bed = ctx.bed(10, 10);
            ctx.decorate(bed, Decor.MACABRE, Decor.HOSTILE, Decor.DARK);
            MockPlayer player = ctx.joinAt("Switcher", bed);
            ctx.night();
            ctx.then(() -> {
                ctx.config().getComfort().nightmareEnabled = false;
                ctx.check(!ComfortCalculator.isNightmare(-5), "nightmares still happen while switched off");
                ctx.config().getComfort().nightmareEnabled = true;
                ctx.check(ComfortCalculator.isNightmare(-5), "nightmares do not happen while switched on");
                ComfortCalculator.invalidateCache(player.get());
                ctx.check(ctx.sleep(player, bed).left().isPresent(), "the bed is not blocked while blocking is on");
                ctx.config().getComfort().sleepBlockEnabled = false;
                ctx.sleepOrFail(player, bed);
            });
        }));

        cases.add(new GameTestCase(ID, "thresholds_are_inclusive", "Nightmare and block thresholds trigger exactly at the value", 200, ctx -> {
            Bed nightmare = ctx.bed(4, 10);
            Bed blocked = ctx.bed(16, 10);
            ctx.decorate(nightmare, Decor.MACABRE);
            ctx.decorate(blocked, Decor.MACABRE, Decor.HOSTILE);
            MockPlayer first = ctx.joinAt("First", nightmare);
            MockPlayer second = ctx.joinAt("Second", blocked);
            ctx.night();
            ctx.then(() -> {
                ctx.check(ComfortCalculator.isNightmare(-1), "comfort -1 must be a nightmare");
                ctx.check(!ComfortCalculator.isNightmare(0), "comfort 0 must not be a nightmare");
                ctx.sleepOrFail(first, nightmare);
                ctx.check(ctx.sleep(second, blocked).left().isPresent(), "comfort -2 must block the bed");
                ctx.config().getComfort().nightmareThreshold = 1;
                ctx.check(ComfortCalculator.isNightmare(1), "comfort 1 must be a nightmare with threshold 1");
                ctx.check(!ComfortCalculator.isNightmare(2), "comfort 2 must not be a nightmare with threshold 1");
            });
        }));

        cases.add(new GameTestCase(ID, "api_event_changes_comfort", "The comfort event from the API can change the result", 100, ctx -> {
            Bed bed = ctx.bed(10, 10);
            MockPlayer player = ctx.joinAt("Modded", bed);
            ctx.then(() -> {
                ApiEventLog.setComfortBonus(3);
                ctx.equal(3, ctx.comfortAt(player, bed), "comfort after the event added 3");
                List<ApiEventLog.Entry> events = ApiEventLog.of("comfort", player.uuid());
                ctx.equal(1, events.size(), "comfort events fired");
                ctx.equal(3, events.getFirst().value(), "comfort seen by the listener");
            });
        }));
    }
}
