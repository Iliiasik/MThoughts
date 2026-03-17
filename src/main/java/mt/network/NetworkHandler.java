package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import mt.network.packet.WellRestedPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class NetworkHandler {

    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(
                SummaryAcknowledgePacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    SummaryAcknowledgePacket packet = SummaryAcknowledgePacket.decode(buf);
                }
        );
    }

    public static void sendDailySummary(ServerPlayerEntity player, DailySummaryPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        DailySummaryPacket.encode(packet, buf);
        ServerPlayNetworking.send(player, DailySummaryPacket.ID, buf);
    }

    public static void sendWellRested(ServerPlayerEntity player, WellRestedPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        WellRestedPacket.encode(packet, buf);
        ServerPlayNetworking.send(player, WellRestedPacket.ID, buf);
    }

    public static void sendSleepingPlayers(ServerPlayerEntity player, SleepingPlayersPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        SleepingPlayersPacket.encode(packet, buf);
        ServerPlayNetworking.send(player, SleepingPlayersPacket.ID, buf);
    }

    public static void sendSummaryAcknowledge(ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        SummaryAcknowledgePacket.encode(new SummaryAcknowledgePacket(), buf);
        ServerPlayNetworking.send(player, SummaryAcknowledgePacket.ID, buf);
    }
}