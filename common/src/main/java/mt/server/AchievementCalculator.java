package mt.server;

import mt.config.MidnightThoughtsConfig;

import java.util.ArrayList;
import java.util.List;

public class AchievementCalculator {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    public static List<String> calculateAchievements(
            DailyPlayerStats.DailyDelta delta,
            StatsStorage.SavedPlayerStats savedStats
    ) {
        List<String> achievementsToAnnounce = new ArrayList<>();

        int distance = delta.distanceWalked();
        int blocks = delta.blocksDestroyed();
        int mobs = delta.mobsKilled();

        if (savedStats.totalSleeps > 0) {
            if (distance > savedStats.recordDistance) {
                achievementsToAnnounce.add("distance_record");
                savedStats.recordDistance = distance;
            }
            if (blocks > savedStats.recordBlocks) {
                achievementsToAnnounce.add("blocks_record");
                savedStats.recordBlocks = blocks;
            }
            if (mobs > savedStats.recordMobs) {
                achievementsToAnnounce.add("mobs_record");
                savedStats.recordMobs = mobs;
            }
        }

        for (AchievementDefinition def : AchievementLoader.load()) {
            if (savedStats.unlockedAchievements.contains(def.id)) continue;
            if (conditionsHold(def.conditions, delta)) {
                achievementsToAnnounce.add(def.id);
                savedStats.unlockedAchievements.add(def.id);
            }
        }

        savedStats.totalSleeps++;

        return achievementsToAnnounce;
    }

    private static boolean conditionsHold(AchievementDefinition.Conditions c, DailyPlayerStats.DailyDelta delta) {
        if (c == null) return false;
        return matches(delta.deaths(), c.deathsEq, c.deathsMin, c.deathsMax)
                && matches(delta.mobsKilled(), c.mobsKilledEq, c.mobsKilledMin, c.mobsKilledMax)
                && matches(delta.blocksDestroyed(), c.blocksDestroyedEq, c.blocksDestroyedMin, c.blocksDestroyedMax)
                && matches(delta.distanceWalked(), c.distanceWalkedEq, c.distanceWalkedMin, c.distanceWalkedMax)
                && matches(delta.jumps(), c.jumpsEq, c.jumpsMin, c.jumpsMax)
                && matches(delta.damageDealt(), c.damageDealtEq, c.damageDealtMin, c.damageDealtMax);
    }

    private static boolean matches(int value, Integer eq, Integer min, Integer max) {
        return (eq == null || value == eq) && (min == null || value >= min) && (max == null || value <= max);
    }

    public static String determineMvp(List<PlayerSummaryData> players) {
        MidnightThoughtsConfig.MvpSettings mvpSettings = CONFIG.getMvp();
        if (!mvpSettings.enabled || players.size() <= 1) return null;

        String mvpName = null;
        int highestScore = 0;
        int mvpCount = 0;

        for (PlayerSummaryData player : players) {
            int score = calculateMvpScore(player);
            if (score > highestScore) {
                highestScore = score;
                mvpName = player.playerName();
                mvpCount = 1;
            } else if (score == highestScore) {
                mvpCount++;
            }
        }

        if (mvpCount > 1 || highestScore < mvpSettings.minScoreRequired) return null;
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
            String playerName, int blocksDestroyed, int distanceWalked,
            int mobsKilled, int deaths, int jumps
    ) {}
}