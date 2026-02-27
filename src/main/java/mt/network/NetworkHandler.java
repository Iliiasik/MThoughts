package mt.network;

import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
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
                .consumerNetworkThread((packet, ctx) -> {
                    ctx.setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(DailySummaryPacket.class)
                .codec(DailySummaryPacket.CODEC)
                .consumerMainThread((packet, ctx) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        Minecraft.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
                    }
                    ctx.setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(SleepingPlayersPacket.class)
                .codec(SleepingPlayersPacket.CODEC)
                .consumerMainThread((packet, ctx) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers());
                    }
                    ctx.setPacketHandled(true);
                })
                .add();
    }

    public static void sendDailySummary(ServerPlayer player, DailySummaryPacket packet) {
        CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }

    public static void sendSleepingPlayers(ServerPlayer player, SleepingPlayersPacket packet) {
        CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }
}