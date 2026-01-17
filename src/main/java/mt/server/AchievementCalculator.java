package mt.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AchievementCalculator {

    public static List<String> calculateAchievements(
            ServerPlayerEntity player,
            DailyPlayerStats.DailyDelta delta,
            MinecraftServer server
    ) {
        List<String> achievements = new ArrayList<>();
        UUID uuid = player.getUuid();

        StatsStorage.SavedPlayerStats saved = StatsStorage.loadPlayerStats(server, uuid);
        if (saved == null) {
            saved = new StatsStorage.SavedPlayerStats();
        }

        int distance = delta.distanceWalked();
        int blocks = delta.blocksDestroyed();
        int mobs = delta.mobsKilled();
        int deaths = delta.deaths();
        int jumps = delta.jumps();

        if (distance > saved.recordDistance && saved.totalSleeps > 0) {
            achievements.add("distance_record");
        }
        if (blocks > saved.recordBlocks && saved.totalSleeps > 0) {
            achievements.add("blocks_record");
        }
        if (mobs > saved.recordMobs && saved.totalSleeps > 0) {
            achievements.add("mobs_record");
        }

        int blocksWalked = distance / 100;

        if (deaths == 0 && mobs >= 10 && blocks >= 50) achievements.add("flawless");
        if (mobs == 0 && blocksWalked >= 5000) achievements.add("pacifist");
        if (mobs >= 50 && deaths <= 1) achievements.add("juggernaut");
        if (blocksWalked >= 30000) achievements.add("marathoner");
        if (jumps >= 500) achievements.add("hyperactive");
        if (blocks >= 1000 && jumps >= 200) achievements.add("demolition_maniac");
        if (blocksWalked >= 15000 && blocks >= 100) achievements.add("explorer");
        if (deaths <= 1 && mobs >= 10) achievements.add("survivor");
        if (blocksWalked >= 10000 && blocks >= 200 && mobs >= 20) achievements.add("combo_master");
        if (deaths == 0 && blocksWalked >= 20000 && blocks >= 200) achievements.add("iron_will");

        updateRecords(server, uuid, delta, saved);

        return achievements;
    }

    private static void updateRecords(MinecraftServer server, UUID uuid, DailyPlayerStats.DailyDelta delta, StatsStorage.SavedPlayerStats saved) {
        if (delta.distanceWalked() > saved.recordDistance) {
            saved.recordDistance = delta.distanceWalked();
        }

        if (delta.blocksDestroyed() > saved.recordBlocks) {
            saved.recordBlocks = delta.blocksDestroyed();
        }

        if (delta.mobsKilled() > saved.recordMobs) {
            saved.recordMobs = delta.mobsKilled();
        }

        saved.totalSleeps++;
        StatsStorage.savePlayerStats(server, uuid, saved);
    }

    public static String determineMvp(List<PlayerSummaryData> players) {
        if (players.size() <= 1) {
            return null;
        }

        String mvpName = null;
        int highestScore = 0;

        for (PlayerSummaryData player : players) {
            int score = calculateMvpScore(player);
            if (score > highestScore) {
                highestScore = score;
                mvpName = player.playerName();
            }
        }

        int mvpCount = 0;
        for (PlayerSummaryData player : players) {
            if (calculateMvpScore(player) == highestScore) {
                mvpCount++;
            }
        }

        if (mvpCount > 1) {
            return null;
        }

        if (highestScore < 10) {
            return null;
        }

        return mvpName;
    }

    private static int calculateMvpScore(PlayerSummaryData player) {
        int score = 0;

        score += player.distanceWalked() / 50;
        score += player.blocksDestroyed() * 3;
        score += player.mobsKilled() * 15;
        score += player.jumps() / 10;
        score -= player.deaths() * 30;

        return Math.max(0, score);
    }

    public record PlayerSummaryData(
        String playerName,
        int blocksDestroyed,
        int distanceWalked,
        int mobsKilled,
        int deaths,
        int jumps
    ) {}
}
