package mt.server;

import mt.network.packet.SleepingPlayersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SleepTracker {
    private static final long NIGHT_START = 12542;
    private static final long MORNING_TIME = 1000;

    private final MinecraftServer server;
    private int lastSleepingCount = -1;
    private int lastTotalPlayers = -1;
    private boolean wasNight = false;
    private long lastTimeOfDay = -1;
    private final Set<UUID> playersWhoSlept = new HashSet<>();
    private final Set<UUID> wakeVoluntarily = new HashSet<>();

    public SleepTracker(MinecraftServer server) {
        this.server = server;
    }

    public void tick() {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        long timeOfDay = overworld.getDayTime() % 24000;
        boolean currentlyNight = isNightTime(timeOfDay);

        int sleepingCount = 0;
        int totalPlayers = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean sleeping = player.isSleeping();
            if (sleeping) sleepingCount++;
            if (!player.isSpectator()) totalPlayers++;
            if (currentlyNight && sleeping) {
                playersWhoSlept.add(player.getUUID());
            }
        }

        if (sleepingCount != lastSleepingCount || totalPlayers != lastTotalPlayers) {
            sendSleepingCountToAllPlayers(sleepingCount, totalPlayers);
            lastSleepingCount = sleepingCount;
            lastTotalPlayers = totalPlayers;
        }

        if (currentlyNight) {
            wasNight = true;
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

    public void markPlayerListChanged() {
        lastSleepingCount = -1;
        lastTotalPlayers = -1;
    }

    public void markPlayerSleeping(UUID uuid) {
        wakeVoluntarily.remove(uuid);
    }

    public void markPlayerWoke(UUID uuid) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;
        long timeOfDay = overworld.getDayTime() % 24000;
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

    private void sendSleepingCountToAllPlayers(int sleeping, int total) {
        PacketDistributor.sendToAllPlayers(new SleepingPlayersPacket(sleeping, total));
    }
}