package mt.server;

import mt.config.MidnightThoughtsConfig;

import java.util.ArrayList;
import java.util.List;

public class AchievementCalculator {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();
    private static final int MAX_SAFE_VALUE = Integer.MAX_VALUE / 2;

    public static List<String> calculateAchievements(
            DailyPlayerStats.DailyDelta delta,
            StatsStorage.SavedPlayerStats savedStats
    ) {
        List<String> achievementsToAnnounce = new ArrayList<>();

        int distance = delta.distanceWalked();
        int blocks = delta.blocksDestroyed();
        int mobs = delta.mobsKilled();
        int deaths = delta.deaths();
        int jumps = delta.jumps();
        int damage = delta.damageDealt();

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

        List<AchievementDefinition> customAchievements = AchievementLoader.load();
        for (AchievementDefinition def : customAchievements) {
            if (savedStats.unlockedAchievements.contains(def.id)) {
                continue;
            }
            if (checkCustomAchievement(def, deaths, mobs, blocks, distance, jumps, damage)) {
                achievementsToAnnounce.add(def.id);
                savedStats.unlockedAchievements.add(def.id);
            }
        }

        savedStats.totalSleeps++;

        return achievementsToAnnounce;
    }

    private static boolean checkCustomAchievement(AchievementDefinition def, int deaths, int mobs, int blocks, int distance, int jumps, int damage) {
        if (def.conditions == null) return false;
        AchievementDefinition.Conditions c = def.conditions;

        if (c.deathsEq != null && deaths != c.deathsEq) return false;
        if (c.deathsMin != null && deaths < c.deathsMin) return false;
        if (c.deathsMax != null && deaths > c.deathsMax) return false;

        if (c.mobsKilledEq != null && mobs != c.mobsKilledEq) return false;
        if (c.mobsKilledMin != null && mobs < c.mobsKilledMin) return false;
        if (c.mobsKilledMax != null && mobs > c.mobsKilledMax) return false;

        if (c.blocksDestroyedEq != null && blocks != c.blocksDestroyedEq) return false;
        if (c.blocksDestroyedMin != null && blocks < c.blocksDestroyedMin) return false;
        if (c.blocksDestroyedMax != null && blocks > c.blocksDestroyedMax) return false;

        if (c.distanceWalkedEq != null && distance != c.distanceWalkedEq) return false;
        if (c.distanceWalkedMin != null && distance < c.distanceWalkedMin) return false;
        if (c.distanceWalkedMax != null && distance > c.distanceWalkedMax) return false;

        if (c.jumpsEq != null && jumps != c.jumpsEq) return false;
        if (c.jumpsMin != null && jumps < c.jumpsMin) return false;
        if (c.jumpsMax != null && jumps > c.jumpsMax) return false;

        if (c.damageDealtEq != null && damage != c.damageDealtEq) return false;
        if (c.damageDealtMin != null && damage < c.damageDealtMin) return false;
        return c.damageDealtMax == null || damage <= c.damageDealtMax;
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
        long score = 0;

        score += (long) safeDivide(player.distanceWalked(), 100) / 100 * mvp.pointsPerDistance100;
        score += (long) player.blocksDestroyed() * mvp.pointsPerBlock;
        score += (long) player.mobsKilled() * mvp.pointsPerMob;
        score += (long) safeDivide(player.jumps(), 10) * mvp.pointsPerJump10;
        score -= (long) player.deaths() * mvp.penaltyPerDeath;

        return clampValue((int) Math.max(0, score));
    }

    private static int safeDivide(int value, int divisor) {
        if (divisor == 0) return 0;
        if (value > MAX_SAFE_VALUE) return MAX_SAFE_VALUE / divisor;
        return value / divisor;
    }

    private static int clampValue(int value) {
        if (value < 0) return 0;
        return Math.min(value, MAX_SAFE_VALUE);
    }

    public record PlayerSummaryData(
            String playerName, int blocksDestroyed, int distanceWalked,
            int mobsKilled, int deaths, int jumps
    ) {}
}