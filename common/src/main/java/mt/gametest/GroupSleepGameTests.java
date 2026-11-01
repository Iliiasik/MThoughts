package mt.gametest;

import mt.gametest.TestContext.Bed;
import mt.gametest.TestContext.Decor;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.PlayerSleepStatePacket;
import mt.server.WellRestedEffect;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

final class GroupSleepGameTests {

    private static final String ID = "group_sleep";

    private GroupSleepGameTests() {}

    private record Room(Bed first, Bed second, Bed third) {
        static Room lit(TestContext ctx) {
            return new Room(SleepGameTests.litBed(ctx, 3, 3), SleepGameTests.litBed(ctx, 17, 3), SleepGameTests.litBed(ctx, 10, 16));
        }
    }

    private static void expectListed(TestContext ctx, MockPlayer viewer, MockPlayer... listed) {
        ctx.check(!ctx.summaries(viewer).isEmpty(), viewer.name() + " got no summary");
        DailySummaryPacket summary = ctx.lastSummary(viewer);
        Set<String> names = summary.summaries().stream()
                .map(DailySummaryPacket.PlayerDailySummary::playerName)
                .collect(Collectors.toSet());
        Set<String> expected = Arrays.stream(listed).map(MockPlayer::name).collect(Collectors.toSet());
        ctx.equal(expected, names, "players listed in the summary of " + viewer.name());
        ctx.equal(1, ctx.summaries(viewer).size(), "summaries received by " + viewer.name());
    }

    private static void expectRested(TestContext ctx, MockPlayer player, boolean rested) {
        ctx.check(WellRestedEffect.hasEffect(player.get()) == rested,
                player.name() + (rested ? " got no Well Rested" : " got Well Rested without sleeping the night"));
    }

    static void register(List<TestCatalog.Category> categories, List<GameTestCase> cases) {
        categories.add(new TestCatalog.Category(ID, "Three players", List.of(
                "one, two or all three of three players sleeping, each summary lists exactly the sleepers",
                "getting up for good, getting up and coming back, and lying down, getting up, lying down and getting up again",
                "a sleeper leaving the game, a player joining at night and a sleeper lying down late",
                "every sleeper keeps the comfort of their own bed: the effect level and the nightmare are per player")));

        cases.add(new GameTestCase(ID, "two_of_three_sleep", "Two of three sleep, both get a summary of the two", 700, ctx -> {
            ctx.sleepingPercentage(50);
            Room room = Room.lit(ctx);
            MockPlayer first = ctx.joinAt("First", room.first());
            MockPlayer second = ctx.joinAt("Second", room.second());
            MockPlayer awake = ctx.joinAt("Awake", room.third());
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(first, room.first());
                ctx.sleepOrFail(second, room.second());
            }).sleepThroughTheNight("the night to pass", first)
                    .then(() -> {
                        expectListed(ctx, first, first, second);
                        expectListed(ctx, second, first, second);
                        SleepGameTests.expectNoSummary(ctx, awake);
                        expectRested(ctx, first, true);
                        expectRested(ctx, second, true);
                        expectRested(ctx, awake, false);
                    });
        }));

        cases.add(new GameTestCase(ID, "one_of_three_sleeps", "One of three sleeps, only they get a summary of one", 700, ctx -> {
            ctx.sleepingPercentage(30);
            Room room = Room.lit(ctx);
            MockPlayer sleeper = ctx.joinAt("Sleeper", room.first());
            MockPlayer second = ctx.joinAt("AwakeOne", room.second());
            MockPlayer third = ctx.joinAt("AwakeTwo", room.third());
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(sleeper, room.first()))
                    .sleepThroughTheNight("the night to pass", sleeper)
                    .then(() -> {
                        expectListed(ctx, sleeper, sleeper);
                        SleepGameTests.expectNoSummary(ctx, second);
                        SleepGameTests.expectNoSummary(ctx, third);
                        expectRested(ctx, sleeper, true);
                        expectRested(ctx, second, false);
                        expectRested(ctx, third, false);
                    });
        }));

        cases.add(new GameTestCase(ID, "all_three_sleep", "All three sleep and everyone gets a summary of three", 700, ctx -> {
            ctx.sleepingPercentage(100);
            Room room = Room.lit(ctx);
            MockPlayer first = ctx.joinAt("First", room.first());
            MockPlayer second = ctx.joinAt("Second", room.second());
            MockPlayer third = ctx.joinAt("Third", room.third());
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(first, room.first());
                ctx.sleepOrFail(second, room.second());
                ctx.sleepOrFail(third, room.third());
            }).sleepThroughTheNight("the night to pass", first)
                    .then(() -> {
                        for (MockPlayer viewer : List.of(first, second, third)) {
                            expectListed(ctx, viewer, first, second, third);
                            expectRested(ctx, viewer, true);
                        }
                    });
        }));

        cases.add(new GameTestCase(ID, "one_of_three_gets_up", "One of three gets up for good, the other two get a summary of two", 700, ctx -> {
            ctx.sleepingPercentage(50);
            Room room = Room.lit(ctx);
            MockPlayer first = ctx.joinAt("First", room.first());
            MockPlayer second = ctx.joinAt("Second", room.second());
            MockPlayer quitter = ctx.joinAt("GotUp", room.third());
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(first, room.first());
                ctx.sleepOrFail(second, room.second());
                ctx.sleepOrFail(quitter, room.third());
            }).idle(20).then(() -> ctx.getUp(quitter))
                    .sleepThroughTheNight("the night to pass", first)
                    .then(() -> {
                        expectListed(ctx, first, first, second);
                        expectListed(ctx, second, first, second);
                        SleepGameTests.expectNoSummary(ctx, quitter);
                        expectRested(ctx, quitter, false);
                    });
        }));

        cases.add(new GameTestCase(ID, "one_of_three_comes_back", "One of three gets up and lies down again, everyone gets a summary of three", 800, ctx -> {
            ctx.sleepingPercentage(100);
            Room room = Room.lit(ctx);
            MockPlayer first = ctx.joinAt("First", room.first());
            MockPlayer second = ctx.joinAt("Second", room.second());
            MockPlayer back = ctx.joinAt("CameBack", room.third());
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(first, room.first());
                ctx.sleepOrFail(second, room.second());
                ctx.sleepOrFail(back, room.third());
            }).idle(20).then(() -> ctx.getUp(back))
                    .idle(20).then(() -> ctx.sleepOrFail(back, room.third()))
                    .sleepThroughTheNight("the night to pass", first)
                    .then(() -> {
                        for (MockPlayer viewer : List.of(first, second, back)) {
                            expectListed(ctx, viewer, first, second, back);
                            expectRested(ctx, viewer, true);
                        }
                    });
        }));

        cases.add(new GameTestCase(ID, "up_down_up", "Lying down, getting up, lying down and getting up again leaves the player out", 800, ctx -> {
            ctx.sleepingPercentage(50);
            Room room = Room.lit(ctx);
            MockPlayer first = ctx.joinAt("First", room.first());
            MockPlayer second = ctx.joinAt("Second", room.second());
            MockPlayer restless = ctx.joinAt("Restless", room.third());
            ctx.night();
            ctx.idle(2).then(() -> ctx.sleepOrFail(restless, room.third()))
                    .idle(10).then(() -> ctx.getUp(restless))
                    .idle(10).then(() -> ctx.sleepOrFail(restless, room.third()))
                    .idle(10).then(() -> {
                        ctx.getUp(restless);
                        ctx.sleepOrFail(first, room.first());
                        ctx.sleepOrFail(second, room.second());
                    })
                    .sleepThroughTheNight("the night to pass", first)
                    .then(() -> {
                        expectListed(ctx, first, first, second);
                        expectListed(ctx, second, first, second);
                        SleepGameTests.expectNoSummary(ctx, restless);
                        expectRested(ctx, restless, false);
                    });
        }));

        cases.add(new GameTestCase(ID, "sleeper_leaves_the_game", "One of three sleepers leaves the game, the other two get a summary of two", 700, ctx -> {
            ctx.sleepingPercentage(100);
            Room room = Room.lit(ctx);
            MockPlayer first = ctx.joinAt("First", room.first());
            MockPlayer second = ctx.joinAt("Second", room.second());
            MockPlayer leaver = ctx.joinAt("Leaver", room.third());
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(first, room.first());
                ctx.sleepOrFail(second, room.second());
                ctx.sleepOrFail(leaver, room.third());
            }).idle(30).then(leaver::leave)
                    .sleepThroughTheNight("the night to pass", first)
                    .then(() -> {
                        expectListed(ctx, first, first, second);
                        expectListed(ctx, second, first, second);
                        SleepGameTests.expectNoSummary(ctx, leaver);
                    });
        }));

        cases.add(new GameTestCase(ID, "third_joins_at_night", "A third player joining at night and staying awake is not in the summary", 700, ctx -> {
            ctx.sleepingPercentage(50);
            Room room = Room.lit(ctx);
            MockPlayer first = ctx.joinAt("First", room.first());
            MockPlayer second = ctx.joinAt("Second", room.second());
            MockPlayer[] late = new MockPlayer[1];
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(first, room.first());
                ctx.sleepOrFail(second, room.second());
            }).idle(30).then(() -> late[0] = ctx.joinAt("Late", room.third()))
                    .sleepThroughTheNight("the night to pass", first)
                    .then(() -> {
                        expectListed(ctx, first, first, second);
                        expectListed(ctx, second, first, second);
                        SleepGameTests.expectNoSummary(ctx, late[0]);
                        expectRested(ctx, late[0], false);
                    });
        }));

        cases.add(new GameTestCase(ID, "late_sleeper_completes_the_night", "Two sleep and wait for a third who lies down late, everyone gets a summary of three", 900, ctx -> {
            ctx.sleepingPercentage(100);
            Room room = Room.lit(ctx);
            MockPlayer first = ctx.joinAt("First", room.first());
            MockPlayer second = ctx.joinAt("Second", room.second());
            MockPlayer late = ctx.joinAt("Late", room.third());
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(first, room.first());
                ctx.sleepOrFail(second, room.second());
            }).idle(120).then(() -> {
                ctx.check(ctx.summaries(first).isEmpty(), "the night passed before the third player lay down");
                ctx.sleepOrFail(late, room.third());
            }).sleepThroughTheNight("the night to pass", late)
                    .then(() -> {
                        for (MockPlayer viewer : List.of(first, second, late)) {
                            expectListed(ctx, viewer, first, second, late);
                            expectRested(ctx, viewer, true);
                        }
                    });
        }));

        cases.add(new GameTestCase(ID, "each_keeps_own_comfort", "Three beds with different comfort give each sleeper their own level and nightmare", 800, ctx -> {
            ctx.sleepingPercentage(100);
            Bed cosy = ctx.bed(3, 3);
            Bed plain = SleepGameTests.litBed(ctx, 17, 3);
            Bed scary = ctx.bed(10, 16);
            ctx.decorate(cosy, Decor.LIGHTING, Decor.CARPET, Decor.FURNITURE, Decor.DECORATION, Decor.STRUCTURE);
            ctx.decorate(scary, Decor.MACABRE);
            MockPlayer comfortable = ctx.joinAt("Cosy", cosy);
            MockPlayer ordinary = ctx.joinAt("Plain", plain);
            MockPlayer haunted = ctx.joinAt("Haunted", scary);
            ctx.night();
            ctx.idle(2).then(() -> {
                ctx.sleepOrFail(comfortable, cosy);
                ctx.sleepOrFail(ordinary, plain);
                ctx.sleepOrFail(haunted, scary);
            }).idle(2).then(() -> {
                ctx.check(!ctx.lastSent(comfortable, PlayerSleepStatePacket.class).nightmareMode(), "Cosy saw a nightmare");
                ctx.check(!ctx.lastSent(ordinary, PlayerSleepStatePacket.class).nightmareMode(), "Plain saw a nightmare");
                ctx.check(ctx.lastSent(haunted, PlayerSleepStatePacket.class).nightmareMode(), "Haunted saw no nightmare");
            }).sleepThroughTheNight("the night to pass", comfortable)
                    .then(() -> {
                        for (MockPlayer viewer : List.of(comfortable, ordinary, haunted)) {
                            expectListed(ctx, viewer, comfortable, ordinary, haunted);
                        }
                        ctx.equal(5, WellRestedEffect.getLevel(comfortable.get()), "Well Rested level of Cosy");
                        ctx.equal(1, WellRestedEffect.getLevel(ordinary.get()), "Well Rested level of Plain");
                        expectRested(ctx, haunted, false);
                    });
        }));
    }
}
