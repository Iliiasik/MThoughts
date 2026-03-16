package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import mt.network.packet.WellRestedPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

        PayloadTypeRegistry.playS2C().register(
                WellRestedPacket.ID,
                WellRestedPacket.CODEC
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

    public static void sendDailySummary(ServerPlayerEntity player, DailySummaryPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendWellRested(ServerPlayerEntity player, WellRestedPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendSleepingPlayers(ServerPlayerEntity player, SleepingPlayersPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }
}