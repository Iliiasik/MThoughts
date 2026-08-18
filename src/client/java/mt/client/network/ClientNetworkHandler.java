package mt.client.network;

import mt.network.NetworkHandler;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;
import java.util.function.Function;

public class ClientNetworkHandler {

    private ClientNetworkHandler() {}

    public static void register() {
        receive(NetworkHandler.DAILY_SUMMARY, DailySummaryPacket::decode, ClientPacketHandlers::handleDailySummary);
        receive(NetworkHandler.WELL_RESTED, WellRestedPacket::decode, ClientPacketHandlers::handleWellRested);
        receive(NetworkHandler.SLEEPING_PLAYERS, SleepingPlayersPacket::decode, ClientPacketHandlers::handleSleepingPlayers);
        receive(NetworkHandler.USER_CONTENT, UserContentPacket::decode, ClientPacketHandlers::handleUserContent);
        receive(NetworkHandler.SYNC_ACHIEVEMENTS, SyncAchievementsPacket::decode, ClientPacketHandlers::handleSyncAchievements);
        receive(NetworkHandler.SYNC_CONFIG, SyncConfigPacket::decode, ClientPacketHandlers::handleSyncConfig);
        receive(NetworkHandler.MOON_PHASE, MoonPhasePacket::decode, ClientPacketHandlers::handleMoonPhase);
    }

    public static void requestMoonPhase() {
        ClientPlayNetworking.send(NetworkHandler.REQUEST_MOON_PHASE, PacketByteBufs.create());
    }

    private static <T> void receive(ResourceLocation channel,
                                    Function<FriendlyByteBuf, T> decoder,
                                    Consumer<T> handler) {
        ClientPlayNetworking.registerGlobalReceiver(channel, (client, listener, buf, sender) -> {
            T packet = decoder.apply(buf);
            client.execute(() -> handler.accept(packet));
        });
    }
}
