package mt.server;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;

import java.util.UUID;

public class DailyPlayerStats {
    private static final int MAX_SAFE_VALUE = Integer.MAX_VALUE / 2;

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
            saved.unlockedAchievements = existing.unlockedAchievements;
        }

        StatsStorage.savePlayerStats(server, playerUuid, saved);
    }

    public void captureCurrentStats(ServerPlayerEntity player) {
        StatHandler stats = player.getStatHandler();

        long blocksDestroyed = 0;
        for (Block block : Registries.BLOCK) {
            blocksDestroyed += stats.getStat(Stats.MINED.getOrCreateStat(block));
            if (blocksDestroyed > MAX_SAFE_VALUE) {
                blocksDestroyed = MAX_SAFE_VALUE;
                break;
            }
        }
        this.baseBlocksDestroyed = (int) blocksDestroyed;

        this.baseDistanceWalked = getTotalDistance(stats);
        this.baseMobsKilled = clampStat(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS)));
        this.baseDeaths = clampStat(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS)));
        this.baseJumps = clampStat(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP)));
        this.baseDamageDealt = clampStat(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT)));
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

        if (total > MAX_SAFE_VALUE) return MAX_SAFE_VALUE;
        return (int) total;
    }

    private int clampStat(int value) {
        if (value < 0) return 0;
        if (value > MAX_SAFE_VALUE) return MAX_SAFE_VALUE;
        return value;
    }

    private int clampDelta(long value) {
        if (value < 0) return 0;
        if (value > MAX_SAFE_VALUE) return MAX_SAFE_VALUE;
        return (int) value;
    }

    public DailyDelta calculateDelta(ServerPlayerEntity player) {
        StatHandler stats = player.getStatHandler();

        long blocksDestroyed = 0;
        for (Block block : Registries.BLOCK) {
            blocksDestroyed += stats.getStat(Stats.MINED.getOrCreateStat(block));
            if (blocksDestroyed > MAX_SAFE_VALUE) {
                blocksDestroyed = MAX_SAFE_VALUE;
                break;
            }
        }

        int currentDistanceWalked = getTotalDistance(stats);
        int currentMobsKilled = clampStat(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS)));
        int currentDeaths = clampStat(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS)));
        int currentJumps = clampStat(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP)));
        int currentDamageDealt = clampStat(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT)));

        int deltaBlocks = clampDelta((long) blocksDestroyed - baseBlocksDestroyed);
        int deltaDistance = clampDelta((long) currentDistanceWalked - baseDistanceWalked);
        int deltaMobs = clampDelta((long) currentMobsKilled - baseMobsKilled);
        int deltaDeaths = clampDelta((long) currentDeaths - baseDeaths);
        int deltaJumps = clampDelta((long) currentJumps - baseJumps);
        int deltaDamageDealt = clampDelta((long) currentDamageDealt - baseDamageDealt);

        return new DailyDelta(deltaBlocks, deltaDistance, deltaMobs, deltaDeaths, deltaJumps, deltaDamageDealt);
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