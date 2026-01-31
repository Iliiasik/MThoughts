package mt.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.StatsCounter;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;

import java.util.UUID;

public class DailyPlayerStats {
    private final UUID playerUuid;
    private int baseBlocksDestroyed = 0;
    private int baseDistanceWalked = 0;
    private int baseMobsKilled = 0;
    private int baseDeaths = 0;
    private int baseJumps = 0;
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
            hasLoadedFromFile = true;
        }
    }

    public void saveToStorage(MinecraftServer server) {
        StatsStorage.SavedPlayerStats existing = StatsStorage.loadPlayerStats(server, playerUuid);
        StatsStorage.SavedPlayerStats saved = new StatsStorage.SavedPlayerStats(
            baseBlocksDestroyed, baseDistanceWalked, baseMobsKilled, baseDeaths, baseJumps
        );
        if (existing != null) {
            saved.recordDistance = existing.recordDistance;
            saved.recordBlocks = existing.recordBlocks;
            saved.recordMobs = existing.recordMobs;
            saved.totalSleeps = existing.totalSleeps;
        }
        StatsStorage.savePlayerStats(server, playerUuid, saved);
    }

    public void captureCurrentStats(ServerPlayer player) {
        StatsCounter stats = player.getStats();
        int blocksDestroyed = 0;
        for (Block block : player.level().registryAccess().registryOrThrow(Registries.BLOCK)) {
            blocksDestroyed += stats.getValue(Stats.BLOCK_MINED.get(block));
        }
        this.baseBlocksDestroyed = blocksDestroyed;
        this.baseDistanceWalked = getTotalDistance(stats);
        this.baseMobsKilled = stats.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        this.baseDeaths = stats.getValue(Stats.CUSTOM.get(Stats.DEATHS));
        this.baseJumps = stats.getValue(Stats.CUSTOM.get(Stats.JUMP));
    }

    private int getTotalDistance(StatsCounter stats) {
        int total = 0;
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
        return total;
    }

    public DailyDelta calculateDelta(ServerPlayer player) {
        StatsCounter stats = player.getStats();
        int blocksDestroyed = 0;
        for (Block block : player.level().registryAccess().registryOrThrow(Registries.BLOCK)) {
            blocksDestroyed += stats.getValue(Stats.BLOCK_MINED.get(block));
        }
        int currentDistanceWalked = getTotalDistance(stats);
        int currentMobsKilled = stats.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        int currentDeaths = stats.getValue(Stats.CUSTOM.get(Stats.DEATHS));
        int currentJumps = stats.getValue(Stats.CUSTOM.get(Stats.JUMP));
        int deltaBlocks = Math.max(0, blocksDestroyed - baseBlocksDestroyed);
        int deltaDistance = Math.max(0, currentDistanceWalked - baseDistanceWalked);
        int deltaMobs = Math.max(0, currentMobsKilled - baseMobsKilled);
        int deltaDeaths = Math.max(0, currentDeaths - baseDeaths);
        int deltaJumps = Math.max(0, currentJumps - baseJumps);
        return new DailyDelta(deltaBlocks, deltaDistance, deltaMobs, deltaDeaths, deltaJumps);
    }

    public record DailyDelta(
        int blocksDestroyed,
        int distanceWalked,
        int mobsKilled,
        int deaths,
        int jumps
    ) {}
}
