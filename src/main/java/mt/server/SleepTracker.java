package mt.server;

import mt.network.packet.SleepingPlayersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SleepTracker {
    private final MinecraftServer server;
    private int lastSleepingCount = -1;
    private int lastTotalPlayers = -1;
    private boolean wasNight = false;
    private final Set<UUID> playersWhoSlept = new HashSet<>();
    private final Set<UUID> wakeVoluntarily = new HashSet<>();
    private final Map<UUID, Integer> pinnedComfort = new HashMap<>();
    private final Map<UUID, Integer> pendingSleep = new HashMap<>();

    public SleepTracker(MinecraftServer server) {
        this.server = server;
    }

    public void tick() {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) return;

        boolean isDay = overworld.isDay();

        boolean hasPendingSleep = !pendingSleep.isEmpty();
        int sleepingCount = 0;
        int totalPlayers = 0;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean sleeping = player.isSleeping();
            if (hasPendingSleep) confirmSleepAttempt(player, sleeping);
            if (sleeping) sleepingCount++;
            if (!player.isSpectator()) totalPlayers++;
            if (!isDay && sleeping) {
                playersWhoSlept.add(player.getUUID());
            }
        }

        if (sleepingCount != lastSleepingCount || totalPlayers != lastTotalPlayers) {
            sendSleepingCountToAllPlayers(sleepingCount, totalPlayers);
            lastSleepingCount = sleepingCount;
            lastTotalPlayers = totalPlayers;
        }

        if (!isDay) {
            wasNight = true;
        } else if (wasNight) {
            wasNight = false;
            Set<UUID> slept = new HashSet<>(playersWhoSlept);
            slept.removeAll(wakeVoluntarily);
            playersWhoSlept.clear();
            wakeVoluntarily.clear();
            DailyStatsManager.showDailySummaryAndReset(server, slept);
            pinnedComfort.clear();
            pendingSleep.clear();
        }
    }

    public void markPlayerListChanged() {
        lastSleepingCount = -1;
        lastTotalPlayers = -1;
    }

    @SuppressWarnings("resource")
    public void markPlayerWoke(ServerPlayer player) {
        if (player.level().isDay()) return;
        wakeVoluntarily.add(player.getUUID());
    }

    public void markSleepAttempt(UUID uuid, int comfortLevel) {
        pendingSleep.put(uuid, comfortLevel);
    }

    private void confirmSleepAttempt(ServerPlayer player, boolean sleeping) {
        Integer comfortLevel = pendingSleep.remove(player.getUUID());
        if (comfortLevel == null || !sleeping) return;

        wakeVoluntarily.remove(player.getUUID());
        pinnedComfort.put(player.getUUID(), comfortLevel);
        if (ComfortCalculator.isNightmare(comfortLevel)) {
            NeoForge.EVENT_BUS.post(new mt.api.event.NightmareEvent(player, comfortLevel));
        }
    }

    public Integer getPinnedComfort(UUID uuid) {
        return pinnedComfort.get(uuid);
    }

    public Integer getSessionComfort(UUID uuid) {
        Integer pinned = pinnedComfort.get(uuid);
        return pinned != null ? pinned : pendingSleep.get(uuid);
    }

    public void forgetPlayer(UUID uuid) {
        pinnedComfort.remove(uuid);
        pendingSleep.remove(uuid);
        wakeVoluntarily.remove(uuid);
        playersWhoSlept.remove(uuid);
    }

    private void sendSleepingCountToAllPlayers(int sleeping, int total) {
        PacketDistributor.sendToAllPlayers(new SleepingPlayersPacket(sleeping, total));
    }
}
