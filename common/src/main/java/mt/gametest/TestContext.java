package mt.gametest;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import mt.config.MidnightThoughtsConfig;
import mt.network.packet.DailySummaryPacket;
import mt.server.ComfortCalculator;
import mt.server.DailyStatsManager;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import mt.network.MTPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

@SuppressWarnings("resource")
public final class TestContext {

    public enum Decor {
        LIGHTING(Blocks.GLOWSTONE),
        CARPET(Blocks.WHITE_CARPET),
        FURNITURE(Blocks.BOOKSHELF),
        DECORATION(Blocks.FLOWER_POT),
        STRUCTURE(Blocks.GLASS),
        MACABRE(Blocks.SKELETON_SKULL),
        HOSTILE(Blocks.NETHERRACK),
        DARK(Blocks.OBSIDIAN);

        private final Block block;

        Decor(Block block) {
            this.block = block;
        }

        public Block block() {
            return block;
        }
    }

    public record Bed(BlockPos foot, BlockPos head) {
        public BlockPos standingSpot() {
            return foot.east();
        }
    }

    public record CommandResult(int result, List<String> output) {
        public boolean mentions(String text) {
            for (String line : output) {
                if (line.contains(text)) return true;
            }
            return false;
        }
    }

    private interface Step {
        boolean tick(long now);
    }

    private static final int[][] DECOR_SLOTS = {
            {-2, -1}, {-2, 0}, {-2, 1}, {-1, 2}, {0, 2}, {1, 2}, {2, 1}, {2, 2}
    };

    private final GameTestHelper helper;
    private final List<MockPlayer> players = new ArrayList<>();
    private final List<Step> steps = new ArrayList<>();
    private int stepIndex;
    private boolean finished;
    private long lastTick;

    TestContext(GameTestHelper helper) {
        this.helper = helper;
    }

    public ServerLevel level() {
        return helper.getLevel();
    }

    public MinecraftServer server() {
        return level().getServer();
    }

    public MidnightThoughtsConfig config() {
        return MidnightThoughtsConfig.getInstance();
    }

    List<MockPlayer> players() {
        return players;
    }

    public BlockPos at(int x, int y, int z) {
        return helper.absolutePos(new BlockPos(x, y + 1, z));
    }

    public void place(BlockPos absolute, Block block) {
        level().setBlock(absolute, block.defaultBlockState(), Block.UPDATE_ALL);
    }

    public void clear(BlockPos absolute) {
        level().setBlock(absolute, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    public Bed bed(int x, int z) {
        BlockPos foot = at(x, 1, z);
        BlockPos head = foot.relative(Direction.SOUTH);
        BlockState base = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH);
        level().setBlock(head, base.setValue(BedBlock.PART, BedPart.HEAD), Block.UPDATE_ALL);
        level().setBlock(foot, base.setValue(BedBlock.PART, BedPart.FOOT), Block.UPDATE_ALL);
        return new Bed(foot, head);
    }

    public void decorate(Bed bed, int slot, Decor decor) {
        int[] offset = DECOR_SLOTS[slot];
        place(bed.head().offset(offset[0], 0, offset[1]), decor.block());
    }

    public void decorate(Bed bed, Decor... decor) {
        for (int i = 0; i < decor.length; i++) {
            decorate(bed, i, decor[i]);
        }
    }

    public void undecorate(Bed bed) {
        for (int[] slot : DECOR_SLOTS) {
            clear(bed.head().offset(slot[0], 0, slot[1]));
        }
    }

    public MockPlayer join(String name) {
        return join(name, at(10, 1, 4), GameType.SURVIVAL);
    }

    public MockPlayer joinAt(String name, Bed bed) {
        return join(name, bed.standingSpot(), GameType.SURVIVAL);
    }

    public void joinAs(String name, GameType gameType) {
        join(name, at(10, 1, 4), gameType);
    }

    private MockPlayer join(String name, BlockPos standAt, GameType gameType) {
        MockPlayer player = MockPlayer.create(name, gameType);
        player.connect(server(), level(), standAt);
        players.add(player);
        return player;
    }

    public MockPlayer rejoin(MockPlayer previous, BlockPos standAt) {
        previous.leave();
        MockPlayer player = previous.sameProfile();
        player.connect(server(), level(), standAt);
        players.add(player);
        return player;
    }

    public void walkTo(MockPlayer player, Bed bed) {
        player.moveTo(level(), bed.standingSpot());
    }

    public Either<Player.BedSleepingProblem, Unit> sleep(MockPlayer player, Bed bed) {
        if (player.get().level() == level()) {
            walkTo(player, bed);
        }
        return player.get().startSleepInBed(bed.head());
    }

    public void sleepOrFail(MockPlayer player, Bed bed) {
        Either<Player.BedSleepingProblem, Unit> result = sleep(player, bed);
        result.ifLeft(problem -> fail(player.name() + " could not lie down: " + problem));
    }

    public void getUp(MockPlayer player) {
        player.get().stopSleepInBed(false, true);
    }

    public int comfortAt(MockPlayer player, Bed bed) {
        ComfortCalculator.invalidateCache(player.get());
        return ComfortCalculator.calculateComfortLevel(player.get(), bed.head());
    }

    public Integer sessionComfort(MockPlayer player) {
        return DailyStatsManager.sessionComfort(player.get());
    }

    public long dayStart() {
        long time = level().getDayTime();
        return time - Math.floorMod(time, 24000L);
    }

    public void setTime(long timeOfDay) {
        level().setDayTime(dayStart() + timeOfDay);
        level().updateSkyBrightness();
    }

    public void night() {
        setTime(18000L);
    }

    public void day() {
        setTime(1000L);
    }

    public void noonThunderstorm() {
        setTime(6000L);
        level().setWeatherParameters(0, 1_000_000, true, true);
        level().setRainLevel(1.0F);
        level().setThunderLevel(1.0F);
        level().updateSkyBrightness();
    }

    public void clearWeather() {
        level().setWeatherParameters(1_000_000, 0, false, false);
        level().setRainLevel(0.0F);
        level().setThunderLevel(0.0F);
        level().updateSkyBrightness();
    }

    public void daylightCycle(boolean enabled) {
        level().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(enabled, server());
    }

    public void weatherCycle(boolean enabled) {
        level().getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(enabled, server());
    }

    public void sleepingPercentage(int percent) {
        level().getGameRules().getRule(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE).set(percent, server());
    }

    public ServerLevel nether() {
        return Objects.requireNonNull(server().getLevel(Level.NETHER), "the nether is not loaded");
    }

    public CommandResult op(String command) {
        return command(command, 4, null);
    }

    public CommandResult opAs(MockPlayer player, String command) {
        return command(command, 4, player.get());
    }

    public CommandResult asPlayer(MockPlayer player, String command) {
        return command(command, 0, player.get());
    }

    private CommandResult command(String command, int permission, ServerPlayer entity) {
        List<String> output = new ArrayList<>();
        CommandSource sink = new CommandSource() {
            @Override
            public void sendSystemMessage(Component component) {
                output.add(component.getString());
            }

            @Override
            public boolean acceptsSuccess() {
                return true;
            }

            @Override
            public boolean acceptsFailure() {
                return true;
            }

            @Override
            public boolean shouldInformAdmins() {
                return false;
            }
        };
        CommandSourceStack source = new CommandSourceStack(sink, Vec3.atCenterOf(at(10, 1, 10)), Vec2.ZERO, level(),
                permission, "GameTest", Component.literal("GameTest"), server(), entity);
        try {
            int result = server().getCommands().getDispatcher().execute(command, source);
            return new CommandResult(result, output);
        } catch (CommandSyntaxException exception) {
            output.add(exception.getMessage());
            return new CommandResult(-1, output);
        }
    }

    public <T extends MTPacket> List<T> sent(MockPlayer player, Class<T> type) {
        return PacketLog.sentTo(player.uuid(), type);
    }

    public <T extends MTPacket> T lastSent(MockPlayer player, Class<T> type) {
        List<T> sent = sent(player, type);
        if (sent.isEmpty()) fail(player.name() + " never received " + type.getSimpleName());
        return sent.get(sent.size() - 1);
    }

    public List<DailySummaryPacket> summaries(MockPlayer player) {
        return sent(player, DailySummaryPacket.class);
    }

    public DailySummaryPacket lastSummary(MockPlayer player) {
        return lastSent(player, DailySummaryPacket.class);
    }

    public DailySummaryPacket.PlayerDailySummary summaryLine(DailySummaryPacket packet, MockPlayer player) {
        return packet.summaries().stream()
                .filter(line -> line.playerName().equals(player.name()))
                .findFirst()
                .orElseThrow(() -> new GameTestAssertException("the summary has no line for " + player.name()));
    }

    public void check(boolean condition, String message) {
        if (!condition) fail(message);
    }

    public void equal(Object expected, Object actual, String what) {
        if (!Objects.equals(expected, actual)) {
            fail(what + ": expected " + expected + ", got " + actual);
        }
    }

    public void near(double expected, double actual, String what) {
        if (Double.isNaN(actual) || Math.abs(expected - actual) > 1.0E-4) {
            fail(what + ": expected " + expected + ", got " + actual);
        }
    }

    public void fail(String message) {
        throw new GameTestAssertException(message);
    }

    public TestContext then(Runnable action) {
        steps.add(now -> {
            action.run();
            return true;
        });
        return this;
    }

    public TestContext idle(int ticks) {
        steps.add(new Step() {
            private long until = -1L;

            @Override
            public boolean tick(long now) {
                if (until < 0L) until = now + ticks;
                return now >= until;
            }
        });
        return this;
    }

    public TestContext waitFor(String what, int maxTicks, BooleanSupplier condition) {
        steps.add(new Step() {
            private long deadline = -1L;

            @Override
            public boolean tick(long now) {
                if (deadline < 0L) deadline = now + maxTicks;
                if (condition.getAsBoolean()) return true;
                if (now >= deadline) fail("gave up waiting for " + what + " after " + maxTicks + " ticks");
                return false;
            }
        });
        return this;
    }

    public TestContext sleepThroughTheNight(String what, MockPlayer sleeper) {
        return waitFor(what, 400, () -> level().isDay() && !sleeper.get().isSleeping()).idle(3);
    }

    void start() {
        helper.onEachTick(this::tick);
    }

    long lastTick() {
        return lastTick;
    }

    private void tick() {
        if (finished) return;
        lastTick = helper.getTick();
        for (MockPlayer player : List.copyOf(players)) {
            player.tick();
        }
        long now = helper.getTick();
        while (stepIndex < steps.size()) {
            if (!steps.get(stepIndex).tick(now)) return;
            stepIndex++;
        }
        finished = true;
        helper.succeed();
    }
}
