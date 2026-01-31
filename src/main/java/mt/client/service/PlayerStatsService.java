package mt.client.service;

import mt.client.model.PlayerSleepStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.stats.Stat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PlayerStatsService {
    public PlayerSleepStats collectStats() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return new PlayerSleepStats();
        }
        PlayerSleepStats sleepStats = new PlayerSleepStats();
        sleepStats.setPlayTimeTicks(player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)));
        sleepStats.setWalkDistanceCm(player.getStats().getValue(Stats.CUSTOM.get(Stats.WALK_ONE_CM)));
        sleepStats.setSprintDistanceCm(player.getStats().getValue(Stats.CUSTOM.get(Stats.SPRINT_ONE_CM)));
        sleepStats.setJumps(player.getStats().getValue(Stats.CUSTOM.get(Stats.JUMP)));
        sleepStats.setMobsKilled(player.getStats().getValue(Stats.CUSTOM.get(Stats.MOB_KILLS)));
        sleepStats.setDeaths(player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS)));
        sleepStats.setSleepInBed(player.getStats().getValue(Stats.CUSTOM.get(Stats.SLEEP_IN_BED)));
        sleepStats.setDamageDealt(player.getStats().getValue(Stats.CUSTOM.get(Stats.DAMAGE_DEALT)));
        sleepStats.setFishCaught(player.getStats().getValue(Stats.CUSTOM.get(Stats.FISH_CAUGHT)));
        int blocksDestroyed = 0;
        for (Stat<Block> blockStat : Stats.BLOCK_MINED) {
            blocksDestroyed += player.getStats().getValue(blockStat);
        }
        sleepStats.setBlocksDestroyed(blocksDestroyed);
        int itemsCrafted = 0;
        for (Stat<Item> craftStat : Stats.ITEM_CRAFTED) {
            itemsCrafted += player.getStats().getValue(craftStat);
        }
        sleepStats.setItemsCrafted(itemsCrafted);

        return sleepStats;
    }
}