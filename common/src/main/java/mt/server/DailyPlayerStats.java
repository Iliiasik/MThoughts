package mt.server;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mt.mixin.StatsCounterAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.stats.Stats;

import java.util.List;
import java.util.UUID;

public class DailyPlayerStats {
    private static final List<ResourceLocation> DISTANCE_STATS = List.of(
            Stats.WALK_ONE_CM, Stats.SPRINT_ONE_CM, Stats.CROUCH_ONE_CM, Stats.SWIM_ONE_CM,
            Stats.FALL_ONE_CM, Stats.CLIMB_ONE_CM, Stats.FLY_ONE_CM, Stats.WALK_UNDER_WATER_ONE_CM,
            Stats.WALK_ON_WATER_ONE_CM, Stats.BOAT_ONE_CM, Stats.PIG_ONE_CM, Stats.HORSE_ONE_CM,
            Stats.MINECART_ONE_CM, Stats.AVIATE_ONE_CM, Stats.STRIDER_ONE_CM);

    private final UUID playerUuid;
    private Totals base = Totals.ZERO;
    private boolean hasLoadedFromFile = false;

    public DailyPlayerStats(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public void loadFromStorage(MinecraftServer server) {
        if (hasLoadedFromFile) return;
        StatsStorage.SavedPlayerStats saved = StatsStorage.loadPlayerStats(server, playerUuid);
        if (saved != null) {
            base = Totals.of(saved);
            hasLoadedFromFile = true;
        }
    }

    public void saveToStorage(MinecraftServer server) {
        StatsStorage.SavedPlayerStats saved = StatsStorage.loadPlayerStats(server, playerUuid);
        if (saved == null) saved = new StatsStorage.SavedPlayerStats();
        base.writeTo(saved);
        StatsStorage.savePlayerStats(server, playerUuid, saved);
    }

    public void captureCurrentStats(ServerPlayer player) {
        base = Totals.of(player.getStats());
    }

    public DailyDelta calculateDelta(ServerPlayer player) {
        return Totals.of(player.getStats()).since(base);
    }

    public record DailyDelta(
            int blocksDestroyed,
            int distanceWalked,
            int mobsKilled,
            int deaths,
            int jumps,
            int damageDealt
    ) {}

    private record Totals(int blocks, int distance, int mobs, int deaths, int jumps, int damage) {
        static final Totals ZERO = new Totals(0, 0, 0, 0, 0, 0);

        static Totals of(StatsCounter stats) {
            return new Totals(
                    blocksMined(stats),
                    distance(stats),
                    custom(stats, Stats.MOB_KILLS),
                    custom(stats, Stats.DEATHS),
                    custom(stats, Stats.JUMP),
                    custom(stats, Stats.DAMAGE_DEALT));
        }

        static Totals of(StatsStorage.SavedPlayerStats saved) {
            return new Totals(saved.baseBlocksDestroyed, saved.baseDistanceWalked, saved.baseMobsKilled,
                    saved.baseDeaths, saved.baseJumps, saved.baseDamageDealt);
        }

        void writeTo(StatsStorage.SavedPlayerStats saved) {
            saved.baseBlocksDestroyed = blocks;
            saved.baseDistanceWalked = distance;
            saved.baseMobsKilled = mobs;
            saved.baseDeaths = deaths;
            saved.baseJumps = jumps;
            saved.baseDamageDealt = damage;
        }

        DailyDelta since(Totals start) {
            return new DailyDelta(
                    Math.max(0, blocks - start.blocks),
                    Math.max(0, distance - start.distance),
                    Math.max(0, mobs - start.mobs),
                    Math.max(0, deaths - start.deaths),
                    Math.max(0, jumps - start.jumps),
                    Math.max(0, damage - start.damage));
        }

        private static int custom(StatsCounter stats, ResourceLocation stat) {
            return stats.getValue(Stats.CUSTOM.get(stat));
        }

        private static int distance(StatsCounter stats) {
            long total = 0;
            for (ResourceLocation stat : DISTANCE_STATS) total += custom(stats, stat);
            return (int) Math.min(total, Integer.MAX_VALUE);
        }

        private static int blocksMined(StatsCounter stats) {
            long total = 0;
            Object2IntMap<Stat<?>> raw = ((StatsCounterAccessor) stats).midnightthoughts$getStats();
            for (Object2IntMap.Entry<Stat<?>> entry : raw.object2IntEntrySet()) {
                if (entry.getKey().getType() == Stats.BLOCK_MINED) total += entry.getIntValue();
            }
            return (int) Math.min(total, Integer.MAX_VALUE);
        }
    }
}
