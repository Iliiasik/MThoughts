package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import mt.network.packet.WellRestedPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHandler {

    public static void registerPackets() {
        PayloadTypeRegistry.clientboundPlay().register(
                DailySummaryPacket.TYPE,
                DailySummaryPacket.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                SleepingPlayersPacket.TYPE,
                SleepingPlayersPacket.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                WellRestedPacket.TYPE,
                WellRestedPacket.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                SummaryAcknowledgePacket.TYPE,
                SummaryAcknowledgePacket.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                SummaryAcknowledgePacket.TYPE,
                (_, _) -> {}
        );
    }

    public static void sendDailySummary(ServerPlayer player, DailySummaryPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendWellRested(ServerPlayer player, WellRestedPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendSleepingPlayers(ServerPlayer player, SleepingPlayersPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }
}