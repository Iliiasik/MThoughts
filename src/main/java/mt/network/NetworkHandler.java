package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import mt.network.packet.WellRestedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                SummaryAcknowledgePacket.TYPE,
                SummaryAcknowledgePacket.CODEC,
                (packet, ctx) -> {}
        );

        registrar.playToClient(
                DailySummaryPacket.TYPE,
                DailySummaryPacket.CODEC,
                (packet, ctx) -> ctx.enqueueWork(() ->
                        mt.client.network.ClientPacketHandlers.handleDailySummary(packet)
                )
        );

        registrar.playToClient(
                SleepingPlayersPacket.TYPE,
                SleepingPlayersPacket.CODEC,
                (packet, ctx) -> ctx.enqueueWork(() ->
                        mt.client.network.ClientPacketHandlers.handleSleepingPlayers(packet)
                )
        );

        registrar.playToClient(
                WellRestedPacket.TYPE,
                WellRestedPacket.CODEC,
                (packet, ctx) -> ctx.enqueueWork(() ->
                        mt.client.network.ClientPacketHandlers.handleWellRested(packet)
                )
        );
    }

    public static void sendDailySummary(ServerPlayer player, DailySummaryPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendSleepingPlayers(ServerPlayer player, SleepingPlayersPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendWellRested(ServerPlayer player, WellRestedPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}