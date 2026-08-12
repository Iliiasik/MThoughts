package mt.platform;

import mt.api.event.ComfortCalculatedEvent;
import mt.api.event.MvpDeterminedEvent;
import mt.api.event.NightmareEvent;
import mt.api.event.WellRestedAppliedEvent;
import mt.api.event.WellRestedExpiredEvent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NeoForgeBridges {

    private NeoForgeBridges() {}

    public static void install() {
        MTEvents.setBridge(new MTEvents.Bridge() {
            @Override
            public int comfortCalculated(ServerPlayer player, int level) {
                ComfortCalculatedEvent event = new ComfortCalculatedEvent(player, level);
                NeoForge.EVENT_BUS.post(event);
                return event.getLevel();
            }

            @Override
            public void nightmare(ServerPlayer player, int comfortLevel) {
                NeoForge.EVENT_BUS.post(new NightmareEvent(player, comfortLevel));
            }

            @Override
            public void wellRestedApplied(ServerPlayer player, int level, boolean mvp, int durationTicks) {
                NeoForge.EVENT_BUS.post(new WellRestedAppliedEvent(player, level, mvp, durationTicks));
            }

            @Override
            public void wellRestedExpired(ServerPlayer player) {
                NeoForge.EVENT_BUS.post(new WellRestedExpiredEvent(player));
            }

            @Override
            public void mvpDetermined(ServerPlayer mvp) {
                NeoForge.EVENT_BUS.post(new MvpDeterminedEvent(mvp));
            }
        });

        MTNetwork.setBridge(new MTNetwork.Bridge() {
            @Override
            public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
                PacketDistributor.sendToPlayer(player, payload);
            }

            @Override
            public void sendToAll(MinecraftServer server, CustomPacketPayload payload) {
                PacketDistributor.sendToAllPlayers(payload);
            }
        });
    }
}
