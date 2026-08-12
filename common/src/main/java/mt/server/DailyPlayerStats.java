package mt.server;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import mt.mixin.StatsCounterAccessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.stats.ServerStatsCounter;

import java.util.UUID;

public class DailyPlayerStats {
    private final UUID playerUuid;
    private int baseBlocksDestroyed = 0;
    private int baseDistanceWalked = 0;
    private int baseMobsKilled = 0;
    private int baseDeaths = 0;
    private int baseJumps = 0;
    private int baseDamageDealt = 0;
    private boolean hasLoadedFromFile = false;

    public DailyPlayerStats(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public void loadFromStorage(MinecraftServer server) {
        if (hasLoadedFromFile) return;

        StatsStorage.SavedPlayerStats saved = StatsStorage.loadPlayerStats(server, playerUuid);
        if (saved != null) {
            this.baseBlocksDestroyed = saved.baseBlocksDestroyed;
            this.baseDistanceWalked = saved.baseDistanceWalked;
            this.baseMobsKilled = saved.baseMobsKilled;
            this.baseDeaths = saved.baseDeaths;
            this.baseJumps = saved.baseJumps;
            this.baseDamageDealt = saved.baseDamageDealt;
            this.hasLoadedFromFile = true;
        }
    }

    public void saveToStorage(MinecraftServer server) {
        StatsStorage.SavedPlayerStats saved = StatsStorage.loadPlayerStats(server, playerUuid);
        if (saved == null) {
            saved = new StatsStorage.SavedPlayerStats();
        }

        saved.baseBlocksDestroyed = this.baseBlocksDestroyed;
        saved.baseDistanceWalked = this.baseDistanceWalked;
        saved.baseMobsKilled = this.baseMobsKilled;
        saved.baseDeaths = this.baseDeaths;
        saved.baseJumps = this.baseJumps;
        saved.baseDamageDealt = this.baseDamageDealt;

        StatsStorage.savePlayerStats(server, playerUuid, saved);
    }

    private static int getTotalBlocksMined(ServerStatsCounter stats) {
        long total = 0;
        Object2IntMap<Stat<?>> raw = ((StatsCounterAccessor) stats).midnightthoughts$getStats();
        for (Object2IntMap.Entry<Stat<?>> entry : raw.object2IntEntrySet()) {
            if (entry.getKey().getType() == Stats.BLOCK_MINED) {
                total += entry.getIntValue();
            }
        }
        return (int) Math.min(total, Integer.MAX_VALUE);
    }

    public void captureCurrentStats(ServerPlayer player) {
        ServerStatsCounter stats = player.getStats();

        this.baseBlocksDestroyed = getTotalBlocksMined(stats);
        this.baseDistanceWalked = getTotalDistance(stats);
        this.baseMobsKilled = stats.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        this.baseDeaths = stats.getValue(Stats.CUSTOM.get(Stats.DEATHS));
        this.baseJumps = stats.getValue(Stats.CUSTOM.get(Stats.JUMP));
        this.baseDamageDealt = stats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT));
    }

    private int getTotalDistance(ServerStatsCounter stats) {
        long total = 0;
        total += stats.getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.CROUCH_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.SWIM_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.FALL_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.CLIMB_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.FLY_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.WALK_UNDER_WATER_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.WALK_ON_WATER_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.BOAT_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.PIG_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.HORSE_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.MINECART_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.AVIATE_ONE_CM));
        total += stats.getValue(Stats.CUSTOM.get(Stats.STRIDER_ONE_CM));
        return (int) Math.min(total, Integer.MAX_VALUE);
    }

    public DailyDelta calculateDelta(ServerPlayer player) {
        ServerStatsCounter stats = player.getStats();

        int deltaBlocks = Math.max(0, getTotalBlocksMined(stats) - baseBlocksDestroyed);
        int deltaDistance = Math.max(0, getTotalDistance(stats) - baseDistanceWalked);
        int deltaMobs = Math.max(0, stats.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS)) - baseMobsKilled);
        int deltaDeaths = Math.max(0, stats.getValue(Stats.CUSTOM.get(Stats.DEATHS)) - baseDeaths);
        int deltaJumps = Math.max(0, stats.getValue(Stats.CUSTOM.get(Stats.JUMP)) - baseJumps);
        int deltaDamageDealt = Math.max(0, stats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT)) - baseDamageDealt);

        return new DailyDelta(deltaBlocks, deltaDistance, deltaMobs, deltaDeaths, deltaJumps, deltaDamageDealt);
    }

    public record DailyDelta(
            int blocksDestroyed, int distanceWalked, int mobsKilled,
            int deaths, int jumps, int damageDealt
    ) {}
}
