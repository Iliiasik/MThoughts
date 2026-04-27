package mt.client.service;

import mt.client.model.PlayerSleepStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;

public class PlayerStatsService {

    public void collectStats() {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null) {
            PlayerSleepStats.empty();
            return;
        }

        StatsCounter stats = player.getStats();

        int blocksDestroyed = 0;
        for (var blockStat : Stats.BLOCK_MINED) {
            blocksDestroyed += stats.getValue(blockStat);
        }

        int itemsCrafted = 0;
        for (var craftStat : Stats.ITEM_CRAFTED) {
            itemsCrafted += stats.getValue(craftStat);
        }

        new PlayerSleepStats(
                stats.getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)),
                stats.getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM)),
                stats.getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM)),
                stats.getValue(Stats.CUSTOM.get(Stats.JUMP)),
                stats.getValue(Stats.CUSTOM.get(Stats.MOB_KILLS)),
                stats.getValue(Stats.CUSTOM.get(Stats.DEATHS)),
                stats.getValue(Stats.CUSTOM.get(Stats.SLEEP_IN_BED)),
                stats.getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT)),
                stats.getValue(Stats.CUSTOM.get(Stats.FISH_CAUGHT)),
                blocksDestroyed,
                itemsCrafted
        );
    }
}