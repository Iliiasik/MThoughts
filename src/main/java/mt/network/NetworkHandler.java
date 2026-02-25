package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class NetworkHandler {
    public static final SimpleChannel CHANNEL = ChannelBuilder
            .named("midnightthoughts:main")
            .networkProtocolVersion(1)
            .simpleChannel();

    public static void registerPackets() {
        CHANNEL.messageBuilder(SummaryAcknowledgePacket.class)
                .codec(SummaryAcknowledgePacket.CODEC)
                .consumerNetworkThread((packet, ctx) -> true)
                .add();

        CHANNEL.messageBuilder(DailySummaryPacket.class)
                .codec(DailySummaryPacket.CODEC)
                .consumerNetworkThread((packet, ctx) -> true)
                .add();

        CHANNEL.messageBuilder(SleepingPlayersPacket.class)
                .codec(SleepingPlayersPacket.CODEC)
                .consumerNetworkThread((packet, ctx) -> true)
                .add();
    }

    public static void sendDailySummary(ServerPlayer player, DailySummaryPacket packet) {
        CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }

    public static void sendSleepingPlayers(ServerPlayer player, SleepingPlayersPacket packet) {
        CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }
}