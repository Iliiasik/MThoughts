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

    public void captureCurrentStats(ServerPlayerEntity player) {
        StatHandler stats = player.getStatHandler();

        long blocksDestroyed = 0;
        for (Block block : Registries.BLOCK) {
            blocksDestroyed += stats.getStat(Stats.MINED.getOrCreateStat(block));
        }
        this.baseBlocksDestroyed = (int) Math.min(blocksDestroyed, Integer.MAX_VALUE);
        this.baseDistanceWalked = getTotalDistance(stats);
        this.baseMobsKilled = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS));
        this.baseDeaths = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
        this.baseJumps = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP));
        this.baseDamageDealt = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT));
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
        return (int) Math.min(total, Integer.MAX_VALUE);
    }

    public DailyDelta calculateDelta(ServerPlayerEntity player) {
        StatHandler stats = player.getStatHandler();

        long blocksDestroyed = 0;
        for (Block block : Registries.BLOCK) {
            blocksDestroyed += stats.getStat(Stats.MINED.getOrCreateStat(block));
        }
        int currentBlocksDestroyed = (int) Math.min(blocksDestroyed, Integer.MAX_VALUE);

        int deltaBlocks = Math.max(0, currentBlocksDestroyed - baseBlocksDestroyed);
        int deltaDistance = Math.max(0, getTotalDistance(stats) - baseDistanceWalked);
        int deltaMobs = Math.max(0, stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS)) - baseMobsKilled);
        int deltaDeaths = Math.max(0, stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS)) - baseDeaths);
        int deltaJumps = Math.max(0, stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP)) - baseJumps);
        int deltaDamageDealt = Math.max(0, stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT)) - baseDamageDealt);

        return new DailyDelta(deltaBlocks, deltaDistance, deltaMobs, deltaDeaths, deltaJumps, deltaDamageDealt);
    }

    public record DailyDelta(
            int blocksDestroyed, int distanceWalked, int mobsKilled,
            int deaths, int jumps, int damageDealt
    ) {}
}