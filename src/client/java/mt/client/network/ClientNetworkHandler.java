package mt.client.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.RequestMoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

public class ClientNetworkHandler {

    private ClientNetworkHandler() {}

    public static void register() {
        receive(DailySummaryPacket.TYPE, ClientPacketHandlers::handleDailySummary);
        receive(WellRestedPacket.TYPE, ClientPacketHandlers::handleWellRested);
        receive(SleepingPlayersPacket.TYPE, ClientPacketHandlers::handleSleepingPlayers);
        receive(UserContentPacket.TYPE, ClientPacketHandlers::handleUserContent);
        receive(SyncAchievementsPacket.TYPE, ClientPacketHandlers::handleSyncAchievements);
        receive(SyncConfigPacket.TYPE, ClientPacketHandlers::handleSyncConfig);
        receive(MoonPhasePacket.TYPE, ClientPacketHandlers::handleMoonPhase);
    }

    public static void requestMoonPhase() {
        ClientPlayNetworking.send(new RequestMoonPhasePacket());
    }

    @SuppressWarnings("resource")
    private static <T extends CustomPacketPayload> void receive(CustomPacketPayload.Type<T> type,
                                                                Consumer<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(type,
                (packet, context) -> context.client().execute(() -> handler.accept(packet)));
    }
}
