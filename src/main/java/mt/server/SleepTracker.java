package mt.server;

import mt.network.NetworkHandler;
import mt.network.packet.SleepingPlayersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SleepTracker {
    private static final long NIGHT_START = 12542;
    private static final long MORNING_TIME = 1000;

    private final MinecraftServer server;
    private int lastSleepingCount = 0;
    private boolean wasNight = false;
    private long lastTimeOfDay = -1;
    private final Set<UUID> playersWhoSlept = new HashSet<>();
    private final Set<UUID> wakeVoluntarily = new HashSet<>();

    public SleepTracker(MinecraftServer server) {
        this.server = server;
    }

    public void tick() {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) return;

        long timeOfDay = overworld.getTimeOfDay() % 24000;
        int sleepingCount = countSleepingPlayers();
        int totalPlayers = countNonSpectatorPlayers();

        if (sleepingCount != lastSleepingCount) {
            sendSleepingCountToAllPlayers(sleepingCount, totalPlayers);
            lastSleepingCount = sleepingCount;
        }

        boolean currentlyNight = isNightTime(timeOfDay);

        if (currentlyNight) {
            wasNight = true;
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.isSleeping()) {
                    playersWhoSlept.add(player.getUuid());
                }
            }
        }

        if (wasNight && !currentlyNight) {
            boolean timeJumped = lastTimeOfDay >= 0 && lastTimeOfDay > timeOfDay;
            boolean morningTransition = lastTimeOfDay >= 0 && isNightTime(lastTimeOfDay) && isMorningTime(timeOfDay);
            if (timeJumped || morningTransition) {
                wasNight = false;
                Set<UUID> slept = new HashSet<>(playersWhoSlept);
                slept.removeAll(wakeVoluntarily);
                playersWhoSlept.clear();
                wakeVoluntarily.clear();
                DailyStatsManager.showDailySummaryAndReset(server, slept);
            }
        }

        lastTimeOfDay = timeOfDay;
    }

    public void markPlayerSleeping(UUID uuid) {
        wakeVoluntarily.remove(uuid);
    }

    public void markPlayerWoke(UUID uuid) {
        ServerWorld overworld = server.getWorld(World.OVERWORLD);
        if (overworld == null) return;
        long timeOfDay = overworld.getTimeOfDay() % 24000;
        if (isNightTime(timeOfDay)) {
            wakeVoluntarily.add(uuid);
        }
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
            if (player.isSleeping()) count++;
        }
        return count;
    }

    private int countNonSpectatorPlayers() {
        int count = 0;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!player.isSpectator()) count++;
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