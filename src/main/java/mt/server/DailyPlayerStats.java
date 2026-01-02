package mt.server;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DailyPlayerStats {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

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
            LOGGER.info("[DailyPlayerStats] Loaded saved base stats for {}: blocks={}, distance={}, mobs={}, deaths={}, jumps={}",
                playerUuid, baseBlocksDestroyed, baseDistanceWalked, baseMobsKilled, baseDeaths, baseJumps);
            hasLoadedFromFile = true;
        }
    }

    public void saveToStorage(MinecraftServer server) {
        StatsStorage.SavedPlayerStats saved = new StatsStorage.SavedPlayerStats(
            baseBlocksDestroyed, baseDistanceWalked, baseMobsKilled, baseDeaths, baseJumps
        );
        StatsStorage.savePlayerStats(server, playerUuid, saved);
    }

    public void captureCurrentStats(ServerPlayerEntity player) {
        StatHandler stats = player.getStatHandler();

        int blocksDestroyed = 0;
        for (Block block : Registries.BLOCK) {
            blocksDestroyed += stats.getStat(Stats.MINED.getOrCreateStat(block));
        }
        this.baseBlocksDestroyed = blocksDestroyed;

        this.baseDistanceWalked = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM));
        this.baseMobsKilled = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS));
        this.baseDeaths = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
        this.baseJumps = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP));

        LOGGER.info("[DailyPlayerStats] Captured base stats for {}: blocks={}, distance={}, mobs={}, deaths={}, jumps={}",
            player.getGameProfile().getName(), baseBlocksDestroyed, baseDistanceWalked, baseMobsKilled, baseDeaths, baseJumps);
    }

    public DailyDelta calculateDelta(ServerPlayerEntity player) {
        StatHandler stats = player.getStatHandler();

        int blocksDestroyed = 0;
        for (Block block : Registries.BLOCK) {
            blocksDestroyed += stats.getStat(Stats.MINED.getOrCreateStat(block));
        }

        int currentDistanceWalked = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM));
        int currentMobsKilled = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS));
        int currentDeaths = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
        int currentJumps = stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP));

        int deltaBlocks = Math.max(0, blocksDestroyed - baseBlocksDestroyed);
        int deltaDistance = Math.max(0, currentDistanceWalked - baseDistanceWalked);
        int deltaMobs = Math.max(0, currentMobsKilled - baseMobsKilled);
        int deltaDeaths = Math.max(0, currentDeaths - baseDeaths);
        int deltaJumps = Math.max(0, currentJumps - baseJumps);

        LOGGER.info("[DailyPlayerStats] Delta for {}: blocks={}, distance={}, mobs={}, deaths={}, jumps={}",
            player.getGameProfile().getName(), deltaBlocks, deltaDistance, deltaMobs, deltaDeaths, deltaJumps);

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


