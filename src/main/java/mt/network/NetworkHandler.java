package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.RequestMoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Objects;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "3";
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
                RequestMoonPhasePacket.class,
                (packet1, buf) -> RequestMoonPhasePacket.encode(),
                buf1 -> RequestMoonPhasePacket.decode(),
                (packet, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayer player = ctx.get().getSender();
                        if (player != null) {
                            ServerLevel overworld = Objects.requireNonNull(player.level().getServer()).getLevel(Level.OVERWORLD);
                            int moonPhase = overworld != null
                                    ? (int) (overworld.getDayTime() / 24000L % 8L)
                                    : 0;
                            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new MoonPhasePacket(moonPhase));
                        }
                    });
                    ctx.get().setPacketHandled(true);
                }
        );

        CHANNEL.registerMessage(
                packetId++,
                DailySummaryPacket.class,
                DailySummaryPacket::encode,
                DailySummaryPacket::decode,
                (packet, ctx) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ctx.get().enqueueWork(() ->
                                mt.client.network.ClientPacketHandlers.handleDailySummary(packet)
                        );
                    }
                    ctx.get().setPacketHandled(true);
                }
        );

        CHANNEL.registerMessage(
                packetId++,
                SleepingPlayersPacket.class,
                SleepingPlayersPacket::encode,
                SleepingPlayersPacket::decode,
                (packet, ctx) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ctx.get().enqueueWork(() ->
                                mt.client.network.ClientPacketHandlers.handleSleepingPlayers(packet)
                        );
                    }
                    ctx.get().setPacketHandled(true);
                }
        );

        CHANNEL.registerMessage(
                packetId++,
                WellRestedPacket.class,
                WellRestedPacket::encode,
                WellRestedPacket::decode,
                (packet, ctx) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ctx.get().enqueueWork(() ->
                                mt.client.network.ClientPacketHandlers.handleWellRested(packet)
                        );
                    }
                    ctx.get().setPacketHandled(true);
                }
        );

        CHANNEL.registerMessage(
                packetId++,
                UserContentPacket.class,
                UserContentPacket::encode,
                UserContentPacket::decode,
                (packet, ctx) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ctx.get().enqueueWork(() ->
                                mt.client.network.ClientPacketHandlers.handleUserContent(packet)
                        );
                    }
                    ctx.get().setPacketHandled(true);
                }
        );

        CHANNEL.registerMessage(
                packetId++,
                SyncAchievementsPacket.class,
                SyncAchievementsPacket::encode,
                SyncAchievementsPacket::decode,
                (packet, ctx) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ctx.get().enqueueWork(() ->
                                mt.client.network.ClientPacketHandlers.handleSyncAchievements(packet)
                        );
                    }
                    ctx.get().setPacketHandled(true);
                }
        );

        CHANNEL.registerMessage(
                packetId++,
                SyncConfigPacket.class,
                SyncConfigPacket::encode,
                SyncConfigPacket::decode,
                (packet, ctx) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ctx.get().enqueueWork(() ->
                                mt.client.network.ClientPacketHandlers.handleSyncConfig(packet)
                        );
                    }
                    ctx.get().setPacketHandled(true);
                }
        );

        CHANNEL.registerMessage(
                packetId++,
                MoonPhasePacket.class,
                MoonPhasePacket::encode,
                MoonPhasePacket::decode,
                (packet, ctx) -> {
                    if (FMLEnvironment.dist == Dist.CLIENT) {
                        ctx.get().enqueueWork(() ->
                                mt.client.network.ClientPacketHandlers.handleMoonPhase(packet)
                        );
                    }
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

    public static void sendSleepingPlayersToAll(SleepingPlayersPacket packet) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendUserContent(ServerPlayer player, UserContentPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendAchievements(ServerPlayer player, SyncAchievementsPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendConfig(ServerPlayer player, SyncConfigPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
