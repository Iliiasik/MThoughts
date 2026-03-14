package mt.network;

import mt.client.manager.WellRestedClientState;
import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import mt.network.packet.WellRestedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath("midnightthoughts", "main"),
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
                    ctx.get().enqueueWork(() ->
                            Minecraft.getInstance().setScreen(new DailySummaryScreen(packet.summaries()))
                    );
                    ctx.get().setPacketHandled(true);
                }
        );
        CHANNEL.registerMessage(
                packetId++,
                SleepingPlayersPacket.class,
                SleepingPlayersPacket::encode,
                SleepingPlayersPacket::decode,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(() ->
                            SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers())
                    );
                    ctx.get().setPacketHandled(true);
                }
        );
        CHANNEL.registerMessage(
                packetId++,
                WellRestedPacket.class,
                WellRestedPacket::encode,
                WellRestedPacket::decode,
                (packet, ctx) -> {
                    ctx.get().enqueueWork(() ->
                            WellRestedClientState.update(packet.active(), packet.level(), packet.ticksRemaining(), packet.totalTicks(), packet.phase(), packet.nightmareMode(), packet.mvp())
                    );
                    ctx.get().setPacketHandled(true);
                }
        );
    }

    public static void sendDailySummary(ServerPlayer player, DailySummaryPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendWellRested(ServerPlayer player, WellRestedPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendSleepingPlayers(ServerPlayer player, SleepingPlayersPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}