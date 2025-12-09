package mt.client.service;

import mt.client.model.PlayerSleepStats;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.stat.StatHandler;
import net.minecraft.stat.Stats;

public class PlayerStatsService {

    public PlayerSleepStats collectStats() {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null) {
            return new PlayerSleepStats();
        }

        StatHandler stats = player.getStatHandler();
        PlayerSleepStats sleepStats = new PlayerSleepStats();

        sleepStats.setPlayTimeTicks(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME)));
        sleepStats.setWalkDistanceCm(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.WALK_ONE_CM)));
        sleepStats.setSprintDistanceCm(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.SPRINT_ONE_CM)));
        sleepStats.setJumps(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.JUMP)));
        sleepStats.setMobsKilled(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.MOB_KILLS)));
        sleepStats.setDeaths(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS)));
        sleepStats.setSleepInBed(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.SLEEP_IN_BED)));
        sleepStats.setDamageDealt(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.DAMAGE_DEALT)));
        sleepStats.setFishCaught(stats.getStat(Stats.CUSTOM.getOrCreateStat(Stats.FISH_CAUGHT)));

        int blocksDestroyed = 0;
        for (var blockStat : Stats.MINED) {
            blocksDestroyed += stats.getStat(blockStat);
        }
        sleepStats.setBlocksDestroyed(blocksDestroyed);

        int itemsCrafted = 0;
        for (var craftStat : Stats.CRAFTED) {
            itemsCrafted += stats.getStat(craftStat);
        }
        sleepStats.setItemsCrafted(itemsCrafted);

        return sleepStats;
    }
}
