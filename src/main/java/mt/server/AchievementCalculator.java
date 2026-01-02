package mt.server;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AchievementCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static final int EXPLORER_THRESHOLD = 5000;
    private static final int MARATHON_THRESHOLD = 20000;
    private static final int NOMAD_THRESHOLD = 50000;
    private static final int MINER_PRO_THRESHOLD = 200;
    private static final int WORKAHOLIC_THRESHOLD = 500;
    private static final int HUNTER_THRESHOLD = 10;
    private static final int MASS_MURDERER_THRESHOLD = 30;
    private static final int JUMPER_THRESHOLD = 200;
    private static final int SURVIVOR_THRESHOLD = 3;
    private static final int CLUMSY_THRESHOLD = 5;

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

        LOGGER.info("[AchievementCalculator] Checking achievements for {}: distance={}, blocks={}, mobs={}, deaths={}, jumps={}, totalSleeps={}",
            player.getGameProfile().getName(), delta.distanceWalked(), delta.blocksDestroyed(),
            delta.mobsKilled(), delta.deaths(), delta.jumps(), saved.totalSleeps);

        if (saved.totalSleeps > 0 && delta.distanceWalked() > saved.recordDistance) {
            achievements.add(DailyAchievement.DISTANCE_RECORD.getId());
            LOGGER.info("[AchievementCalculator] {} earned DISTANCE_RECORD (new: {}, old: {})",
                player.getGameProfile().getName(), delta.distanceWalked(), saved.recordDistance);
        }

        if (saved.totalSleeps > 0 && delta.blocksDestroyed() > saved.recordBlocks) {
            achievements.add(DailyAchievement.BLOCKS_RECORD.getId());
            LOGGER.info("[AchievementCalculator] {} earned BLOCKS_RECORD (new: {}, old: {})",
                player.getGameProfile().getName(), delta.blocksDestroyed(), saved.recordBlocks);
        }

        if (saved.totalSleeps > 0 && delta.mobsKilled() > saved.recordMobs) {
            achievements.add(DailyAchievement.MOBS_RECORD.getId());
            LOGGER.info("[AchievementCalculator] {} earned MOBS_RECORD (new: {}, old: {})",
                player.getGameProfile().getName(), delta.mobsKilled(), saved.recordMobs);
        }

        if (delta.distanceWalked() >= NOMAD_THRESHOLD) {
            achievements.add(DailyAchievement.NOMAD.getId());
        } else if (delta.distanceWalked() >= MARATHON_THRESHOLD) {
            achievements.add(DailyAchievement.MARATHON.getId());
        } else if (delta.distanceWalked() >= EXPLORER_THRESHOLD) {
            achievements.add(DailyAchievement.EXPLORER.getId());
        }

        if (delta.blocksDestroyed() >= WORKAHOLIC_THRESHOLD) {
            achievements.add(DailyAchievement.WORKAHOLIC.getId());
        } else if (delta.blocksDestroyed() >= MINER_PRO_THRESHOLD) {
            achievements.add(DailyAchievement.MINER_PRO.getId());
        }

        if (delta.mobsKilled() >= MASS_MURDERER_THRESHOLD) {
            achievements.add(DailyAchievement.MASS_MURDERER.getId());
        } else if (delta.mobsKilled() >= HUNTER_THRESHOLD) {
            achievements.add(DailyAchievement.HUNTER.getId());
        }

        if (delta.mobsKilled() > 0 && saved.recordMobs == 0 && saved.totalSleeps > 0) {
            achievements.add(DailyAchievement.FIRST_BLOOD.getId());
        }

        if (delta.deaths() == 0 && (delta.blocksDestroyed() > 50 || delta.distanceWalked() > 2000 || delta.mobsKilled() > 3)) {
            achievements.add(DailyAchievement.DEATHLESS.getId());
        }

        if (delta.deaths() >= CLUMSY_THRESHOLD) {
            achievements.add(DailyAchievement.CLUMSY.getId());
        } else if (delta.deaths() >= SURVIVOR_THRESHOLD && delta.mobsKilled() > delta.deaths()) {
            achievements.add(DailyAchievement.SURVIVOR.getId());
        }

        if (delta.deaths() == 0 && delta.mobsKilled() >= 5) {
            achievements.add(DailyAchievement.UNTOUCHABLE.getId());
        }

        if (delta.jumps() >= JUMPER_THRESHOLD) {
            achievements.add(DailyAchievement.JUMPER.getId());
        }

        if (delta.mobsKilled() == 0 && delta.distanceWalked() >= 3000) {
            achievements.add(DailyAchievement.PACIFIST.getId());
        }

        if (delta.distanceWalked() >= 10000 && delta.blocksDestroyed() >= 100 && delta.mobsKilled() >= 5) {
            achievements.add(DailyAchievement.SPEEDRUNNER.getId());
        }

        if (delta.distanceWalked() < 500 && delta.blocksDestroyed() < 20 && delta.jumps() < 50) {
            achievements.add(DailyAchievement.LAZY.getId());
        }

        if (delta.jumps() >= 500) {
            achievements.add(DailyAchievement.HYPERACTIVE.getId());
        }

        if (delta.blocksDestroyed() >= 1000) {
            achievements.add(DailyAchievement.DEMOLITION_EXPERT.getId());
        }

        if (delta.jumps() >= 100 && delta.distanceWalked() >= 5000) {
            achievements.add(DailyAchievement.BUNNY_HOP.getId());
        }

        if (delta.deaths() == 0 && delta.distanceWalked() >= 10000) {
            achievements.add(DailyAchievement.IRON_WILL.getId());
        }

        if (delta.mobsKilled() >= 50) {
            achievements.add(DailyAchievement.BLOODTHIRSTY.getId());
        }

        if (delta.deaths() <= 1 && delta.blocksDestroyed() >= 300) {
            achievements.add(DailyAchievement.CAREFUL.getId());
        }

        if (delta.distanceWalked() >= 8000 && delta.mobsKilled() >= 10 && delta.blocksDestroyed() >= 50) {
            achievements.add(DailyAchievement.ADVENTURER.getId());
        }

        LOGGER.info("[AchievementCalculator] {} earned {} achievements: {}",
            player.getGameProfile().getName(), achievements.size(), achievements);

        updateRecords(server, uuid, delta, saved);

        return achievements;
    }

    private static void updateRecords(MinecraftServer server, UUID uuid, DailyPlayerStats.DailyDelta delta, StatsStorage.SavedPlayerStats saved) {
        if (delta.distanceWalked() > saved.recordDistance) {
            saved.recordDistance = delta.distanceWalked();
            LOGGER.info("[AchievementCalculator] New distance record for {}: {}", uuid, saved.recordDistance);
        }

        if (delta.blocksDestroyed() > saved.recordBlocks) {
            saved.recordBlocks = delta.blocksDestroyed();
            LOGGER.info("[AchievementCalculator] New blocks record for {}: {}", uuid, saved.recordBlocks);
        }

        if (delta.mobsKilled() > saved.recordMobs) {
            saved.recordMobs = delta.mobsKilled();
            LOGGER.info("[AchievementCalculator] New mobs record for {}: {}", uuid, saved.recordMobs);
        }

        saved.totalSleeps++;
        StatsStorage.savePlayerStats(server, uuid, saved);
        LOGGER.info("[AchievementCalculator] Saved records for {}: distance={}, blocks={}, mobs={}, sleeps={}",
            uuid, saved.recordDistance, saved.recordBlocks, saved.recordMobs, saved.totalSleeps);
    }

    public static String determineMvp(List<PlayerSummaryData> players) {
        if (players.size() <= 1) {
            return null;
        }

        String mvpName = null;
        int highestScore = 0;

        for (PlayerSummaryData player : players) {
            int score = calculateMvpScore(player);
            LOGGER.info("[AchievementCalculator] MVP score for {}: {}", player.playerName(), score);
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
            LOGGER.info("[AchievementCalculator] No MVP - tie between {} players", mvpCount);
            return null;
        }

        if (highestScore < 10) {
            LOGGER.info("[AchievementCalculator] No MVP - highest score {} below threshold", highestScore);
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
