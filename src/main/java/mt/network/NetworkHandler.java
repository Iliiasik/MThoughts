package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.resources.ResourceLocation;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("midnightthoughts", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int packetId = 0;

    public static void registerPackets() {
        CHANNEL.registerMessage(
                packetId++,
                SummaryAcknowledgePacket.class,
                SummaryAcknowledgePacket::encode,
                SummaryAcknowledgePacket::decode,
                (packet, ctx) -> {
                    ctx.get().setPacketHandled(true);
                }
        );
        CHANNEL.registerMessage(
                packetId++,
                DailySummaryPacket.class,
                DailySummaryPacket::encode,
                DailySummaryPacket::decode,
                (packet, ctx) -> {
                    ctx.get().setPacketHandled(true);
                }
        );
        CHANNEL.registerMessage(
                packetId++,
                SleepingPlayersPacket.class,
                SleepingPlayersPacket::encode,
                SleepingPlayersPacket::decode,
                (packet, ctx) -> {
                    ctx.get().setPacketHandled(true);
                }
        );
    }

    public static void sendDailySummary(ServerPlayer player, DailySummaryPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendSleepingPlayers(ServerPlayer player, SleepingPlayersPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendSummaryAcknowledge(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SummaryAcknowledgePacket());
    }
}
