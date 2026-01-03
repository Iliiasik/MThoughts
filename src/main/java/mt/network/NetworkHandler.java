package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class NetworkHandler {

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(
            DailySummaryPacket.ID,
            DailySummaryPacket.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
            SleepingPlayersPacket.ID,
            SleepingPlayersPacket.CODEC
        );

        PayloadTypeRegistry.playC2S().register(
            SummaryAcknowledgePacket.ID,
            SummaryAcknowledgePacket.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
            SummaryAcknowledgePacket.ID,
            (packet, context) -> {}
        );
    }

    public static void sendToClient(ServerPlayerEntity player, CustomPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }
}
