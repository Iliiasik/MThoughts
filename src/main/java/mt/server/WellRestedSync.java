package mt.server;

import mt.network.NetworkHandler;
import mt.network.packet.WellRestedPacket;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WellRestedSync {
    private static final int RESYNC_INTERVAL_TICKS = 20;
    private static final Map<UUID, Snapshot> lastSent = new ConcurrentHashMap<>();

    private record Snapshot(boolean active, int level, int totalTicks, int phase,
                            boolean nightmare, boolean mvp, long lastSendTick) {
        boolean sameState(boolean active, int level, int totalTicks, int phase, boolean nightmare, boolean mvp) {
            return this.active == active && this.level == level && this.totalTicks == totalTicks
                    && this.phase == phase && this.nightmare == nightmare && this.mvp == mvp;
        }
    }

    private WellRestedSync() {}

    public static void sync(ServerPlayerEntity player) {
        boolean active = WellRestedEffect.hasEffect(player);
        int level = WellRestedEffect.getLevel(player);
        int ticksRemaining = WellRestedEffect.getTicksRemaining(player);
        int totalTicks = active ? WellRestedEffect.getTotalDurationTicksForPlayer(player) : 0;
        int phase = WellRestedEffect.getCurrentPhase(player);
        boolean nightmare = player.isSleeping() && ComfortCalculator.isNightmareMode(player);
        boolean mvp = WellRestedEffect.isMvp(player);

        long now = player.getEntityWorld().getTime();
        UUID uuid = player.getUuid();
        Snapshot prev = lastSent.get(uuid);

        boolean stateChanged = prev == null || !prev.sameState(active, level, totalTicks, phase, nightmare, mvp);
        boolean resyncDue = prev == null || (now - prev.lastSendTick()) >= RESYNC_INTERVAL_TICKS;

        if (stateChanged || (active && resyncDue)) {
            NetworkHandler.sendWellRested(player,
                    new WellRestedPacket(active, level, ticksRemaining, totalTicks, phase, nightmare, mvp));
            lastSent.put(uuid, new Snapshot(active, level, totalTicks, phase, nightmare, mvp, now));
        }
    }

    public static void clear(UUID uuid) {
        lastSent.remove(uuid);
    }
}