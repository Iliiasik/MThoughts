package mt.client.network;

import mt.network.MTPacket;
import mt.network.NetworkHandler;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import mt.platform.MTClientNetwork;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;

public final class ClientNetworkHandler {

    private ClientNetworkHandler() {}

    public static void register() {
        receive(DailySummaryPacket.ID, DailySummaryPacket::read, ClientPacketHandlers::handleDailySummary);
        receive(WellRestedPacket.ID, WellRestedPacket::read, ClientPacketHandlers::handleWellRested);
        receive(SleepingPlayersPacket.ID, SleepingPlayersPacket::read, ClientPacketHandlers::handleSleepingPlayers);
        receive(UserContentPacket.ID, UserContentPacket::read, ClientPacketHandlers::handleUserContent);
        receive(SyncAchievementsPacket.ID, SyncAchievementsPacket::read, ClientPacketHandlers::handleSyncAchievements);
        receive(SyncConfigPacket.ID, SyncConfigPacket::read, ClientPacketHandlers::handleSyncConfig);
        receive(MoonPhasePacket.ID, MoonPhasePacket::read, ClientPacketHandlers::handleMoonPhase);

        MTClientNetwork.setBridge(ClientNetworkHandler::sendToServer);
    }

    private static void sendToServer(MTPacket packet) {
        ClientPlayNetworking.send(packet.id(), NetworkHandler.encode(packet));
    }

    private static <T extends MTPacket> void receive(ResourceLocation id, Function<FriendlyByteBuf, T> reader,
                                                     Consumer<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(id, (client, listener, buf, responseSender) -> {
            T packet = reader.apply(buf);
            client.execute(() -> handler.accept(packet));
        });
    }
}
