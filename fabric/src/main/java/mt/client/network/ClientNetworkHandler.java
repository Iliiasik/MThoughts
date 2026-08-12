package mt.client.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import mt.platform.MTClientNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class ClientNetworkHandler {

    private ClientNetworkHandler() {}

    @SuppressWarnings("resource")
    public static void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(WellRestedPacket.TYPE,
                (packet, context) -> context.client().execute(() -> ClientPacketHandlers.handleWellRested(packet)));

        ClientPlayNetworking.registerGlobalReceiver(DailySummaryPacket.TYPE,
                (packet, context) -> context.client().execute(() -> ClientPacketHandlers.handleDailySummary(packet)));

        ClientPlayNetworking.registerGlobalReceiver(SleepingPlayersPacket.TYPE,
                (packet, context) -> context.client().execute(() -> ClientPacketHandlers.handleSleepingPlayers(packet)));

        ClientPlayNetworking.registerGlobalReceiver(UserContentPacket.TYPE,
                (packet, context) -> context.client().execute(() -> ClientPacketHandlers.handleUserContent(packet)));

        ClientPlayNetworking.registerGlobalReceiver(SyncAchievementsPacket.TYPE,
                (packet, context) -> context.client().execute(() -> ClientPacketHandlers.handleSyncAchievements(packet)));

        ClientPlayNetworking.registerGlobalReceiver(SyncConfigPacket.TYPE,
                (packet, context) -> context.client().execute(() -> ClientPacketHandlers.handleSyncConfig(packet)));

        ClientPlayNetworking.registerGlobalReceiver(MoonPhasePacket.TYPE,
                (packet, context) -> context.client().execute(() -> ClientPacketHandlers.handleMoonPhase(packet)));
    }

    public static void installBridge() {
        MTClientNetwork.setBridge(ClientPlayNetworking::send);
    }
}
