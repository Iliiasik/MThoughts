package mt.server;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AchievementCalculator {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

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

        checkAchievement(achievements, "flawless", deaths, mobs, blocks, blocksWalked, jumps);
        checkAchievement(achievements, "pacifist", deaths, mobs, blocks, blocksWalked, jumps);
        checkAchievement(achievements, "juggernaut", deaths, mobs, blocks, blocksWalked, jumps);
        checkAchievement(achievements, "marathoner", deaths, mobs, blocks, blocksWalked, jumps);
        checkAchievement(achievements, "hyperactive", deaths, mobs, blocks, blocksWalked, jumps);
        checkAchievement(achievements, "demolition_maniac", deaths, mobs, blocks, blocksWalked, jumps);
        checkAchievement(achievements, "explorer", deaths, mobs, blocks, blocksWalked, jumps);
        checkAchievement(achievements, "survivor", deaths, mobs, blocks, blocksWalked, jumps);
        checkAchievement(achievements, "combo_master", deaths, mobs, blocks, blocksWalked, jumps);
        checkAchievement(achievements, "iron_will", deaths, mobs, blocks, blocksWalked, jumps);

        updateRecords(server, uuid, delta, saved);

        return achievements;
    }

    private static void checkAchievement(List<String> achievements, String achievementId, int deaths, int mobs, int blocks, int blocksWalked, int jumps) {
        MidnightThoughtsConfig.AchievementRequirement req = CONFIG.getAchievements().getRequirement(achievementId);

        if (req.deaths != null && deaths != req.deaths) return;
        if (req.deathsMax != null && deaths > req.deathsMax) return;
        if (req.mobsMin != null && mobs < req.mobsMin) return;
        if (req.mobsMax != null && mobs > req.mobsMax) return;
        if (req.blocksMin != null && blocks < req.blocksMin) return;
        if (req.distanceMin != null && blocksWalked < req.distanceMin) return;
        if (req.jumpsMin != null && jumps < req.jumpsMin) return;

        achievements.add(achievementId);
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
        MidnightThoughtsConfig.MvpSettings mvpSettings = CONFIG.getMvp();

        if (!mvpSettings.enabled || players.size() <= 1) {
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

        if (highestScore < mvpSettings.minScoreRequired) {
            return null;
        }

        return mvpName;
    }

    private static int calculateMvpScore(PlayerSummaryData player) {
        MidnightThoughtsConfig.MvpSettings mvp = CONFIG.getMvp();
        int score = 0;

        score += (player.distanceWalked() / 100) / 100 * mvp.pointsPerDistance100;
        score += player.blocksDestroyed() * mvp.pointsPerBlock;
        score += player.mobsKilled() * mvp.pointsPerMob;
        score += player.jumps() / 10 * mvp.pointsPerJump10;
        score -= player.deaths() * mvp.penaltyPerDeath;

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
