package mt.network;

import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
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
            (packet, ctx) -> ctx.enqueueWork(() -> Minecraft.getInstance().setScreen(new DailySummaryScreen(packet.summaries())))
        );

        registrar.playToClient(
            SleepingPlayersPacket.TYPE,
            SleepingPlayersPacket.CODEC,
            (packet, ctx) -> ctx.enqueueWork(() -> SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers()))
        );
    }

    public static void sendDailySummary(ServerPlayer player, DailySummaryPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendSleepingPlayers(ServerPlayer player, SleepingPlayersPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendToServer(CustomPacketPayload packet) {
        ClientPacketDistributor.sendToServer(packet);
    }
}