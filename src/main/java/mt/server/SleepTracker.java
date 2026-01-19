package mt.server;

import mt.network.NetworkHandler;
import mt.network.packet.SleepingPlayersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class SleepTracker {
    private static final long NIGHT_START = 12542;
    private static final long MORNING_TIME = 1000;

    private final MinecraftServer server;
    private int lastSleepingCount = 0;
    private boolean allPlayersSleeping = false;
    private boolean wasNightBeforeSleep = false;
    private long lastTimeOfDay = -1;

    public SleepTracker(MinecraftServer server) {
        this.server = server;
    }

    public void tick() {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) {
            return;
        }

        long timeOfDay = overworld.getTimeOfDay() % 24000;
        int sleepingCount = countSleepingPlayers();
        int totalPlayers = countNonSpectatorPlayers();

        if (sleepingCount != lastSleepingCount) {
            sendSleepingCountToAllPlayers(sleepingCount, totalPlayers);
            lastSleepingCount = sleepingCount;
        }

        boolean allSleepingNow = totalPlayers > 0 && sleepingCount == totalPlayers;

        if (allSleepingNow && !allPlayersSleeping) {
            wasNightBeforeSleep = isNightTime(timeOfDay);
        }

        if (allPlayersSleeping && sleepingCount == 0) {
            boolean isMorningNow = isMorningTime(timeOfDay);
            boolean wasNightLastTick = isNightTime(lastTimeOfDay);
            boolean timeJumped = lastTimeOfDay >= 0 && (lastTimeOfDay > timeOfDay || (wasNightLastTick && isMorningNow));

            if (wasNightBeforeSleep && (timeJumped || isMorningNow)) {
                DailyStatsManager.showDailySummaryAndReset(server);
            }

            wasNightBeforeSleep = false;
        }

        allPlayersSleeping = allSleepingNow;
        lastTimeOfDay = timeOfDay;
    }

    private boolean isNightTime(long time) {
        return time >= NIGHT_START || time < 0;
    }

    private boolean isMorningTime(long time) {
        return time >= 0 && time <= MORNING_TIME + 500;
    }

    private int countSleepingPlayers() {
        int count = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.isSleeping()) {
                count++;
            }
        }
        return count;
    }

    private int countNonSpectatorPlayers() {
        int count = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!player.isSpectator()) {
                count++;
            }
        }
        return count;
    }

    private void sendSleepingCountToAllPlayers(int sleeping, int total) {
        SleepingPlayersPacket packet = new SleepingPlayersPacket(sleeping, total);
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            NetworkHandler.sendSleepingPlayers(player, packet);
        }
    }
}