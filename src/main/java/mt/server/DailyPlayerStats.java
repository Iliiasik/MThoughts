package mt.server;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;

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

    public void captureCurrentStats(ServerPlayerEntity player) {
        StatHandler stats = player.getStatHandler();

        long blocksDestroyed = 0;
        for (Block block : Registries.BLOCK) {
            blocksDestroyed += stats.getStat(Stats.MINED.getOrCreateStat(block));
        }
        this.baseBlocksDestroyed = (blocksDestroyed > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) blocksDestroyed;

        this.baseDistanceWalked = getTotalDistance(stats);
        this.baseMobsKilled = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS));
        this.baseDeaths = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
        this.baseJumps = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP));
    }

    private int getTotalDistance(StatHandler stats) {
        long total = 0;
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.SPRINT_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.CROUCH_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.SWIM_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.FALL_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.CLIMB_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.FLY_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_UNDER_WATER_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ON_WATER_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.BOAT_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PIG_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.HORSE_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MINECART_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.AVIATE_ONE_CM));
        total += stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.STRIDER_ONE_CM));

        if (total > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) total;
    }

    public DailyDelta calculateDelta(ServerPlayerEntity player) {
        StatHandler stats = player.getStatHandler();

        long blocksDestroyed = 0;
        for (Block block : Registries.BLOCK) {
            blocksDestroyed += stats.getStat(Stats.MINED.getOrCreateStat(block));
        }
        int currentBlocksDestroyed = (blocksDestroyed > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) blocksDestroyed;

        int currentDistanceWalked = getTotalDistance(stats);
        int currentMobsKilled = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS));
        int currentDeaths = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
        int currentJumps = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP));

        int deltaBlocks = clampDelta(currentBlocksDestroyed - baseBlocksDestroyed);
        int deltaDistance = clampDelta(currentDistanceWalked - baseDistanceWalked);
        int deltaMobs = clampDelta(currentMobsKilled - baseMobsKilled);
        int deltaDeaths = clampDelta(currentDeaths - baseDeaths);
        int deltaJumps = clampDelta(currentJumps - baseJumps);

        return new DailyDelta(deltaBlocks, deltaDistance, deltaMobs, deltaDeaths, deltaJumps);
    }

    private int clampDelta(int delta) {
        if (delta < 0) {
            return 0;
        }
        return Math.min(delta, Integer.MAX_VALUE / 2);
    }

    public record DailyDelta(
        int blocksDestroyed,
        int distanceWalked,
        int mobsKilled,
        int deaths,
        int jumps
    ) {}
}


