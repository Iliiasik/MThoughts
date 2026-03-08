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
            hasLoadedFromFile = true;
        }
    }

    public void saveToStorage(MinecraftServer server) {
        StatsStorage.SavedPlayerStats existing = StatsStorage.loadPlayerStats(server, playerUuid);

        StatsStorage.SavedPlayerStats saved = new StatsStorage.SavedPlayerStats(
            baseBlocksDestroyed, baseDistanceWalked, baseMobsKilled, baseDeaths, baseJumps, baseDamageDealt
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

        long blocksDestroyed = 0;
        for (Block block : player.level().registryAccess().lookupOrThrow(Registries.BLOCK)) {
            blocksDestroyed += stats.getValue(Stats.BLOCK_MINED.get(block));
        }
        this.baseBlocksDestroyed = (blocksDestroyed > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) blocksDestroyed;

        this.baseDistanceWalked = getTotalDistance(stats);
        this.baseMobsKilled = stats.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        this.baseDeaths = stats.getValue(Stats.CUSTOM.get(Stats.DEATHS));
        this.baseJumps = stats.getValue(Stats.CUSTOM.get(Stats.JUMP));
        this.baseDamageDealt = stats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT));
    }

    private int getTotalDistance(StatsCounter stats) {
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

        if (total > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        return (int) total;
    }

    public DailyDelta calculateDelta(ServerPlayer player) {
        StatsCounter stats = player.getStats();

        long blocksDestroyed = 0;
        for (Block block : player.level().registryAccess().lookupOrThrow(Registries.BLOCK)) {
            blocksDestroyed += stats.getValue(Stats.BLOCK_MINED.get(block));
        }
        int currentBlocksDestroyed = (blocksDestroyed > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) blocksDestroyed;

        int currentDistanceWalked = getTotalDistance(stats);
        int currentMobsKilled = stats.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS));
        int currentDeaths = stats.getValue(Stats.CUSTOM.get(Stats.DEATHS));
        int currentJumps = stats.getValue(Stats.CUSTOM.get(Stats.JUMP));
        int currentDamageDealt = stats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT));

        int deltaBlocks = clampDelta(currentBlocksDestroyed - baseBlocksDestroyed);
        int deltaDistance = clampDelta(currentDistanceWalked - baseDistanceWalked);
        int deltaMobs = clampDelta(currentMobsKilled - baseMobsKilled);
        int deltaDeaths = clampDelta(currentDeaths - baseDeaths);
        int deltaJumps = clampDelta(currentJumps - baseJumps);
        int deltaDamageDealt = clampDelta(currentDamageDealt - baseDamageDealt);

        return new DailyDelta(deltaBlocks, deltaDistance, deltaMobs, deltaDeaths, deltaJumps, deltaDamageDealt);
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
        int jumps,
        int damageDealt
    ) {}
}
