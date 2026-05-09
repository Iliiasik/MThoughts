package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.RequestMoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Objects;

public class NetworkHandler {

    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                SummaryAcknowledgePacket.TYPE,
                SummaryAcknowledgePacket.CODEC,
                (packet, ctx) -> {}
        );

        registrar.playToServer(
                RequestMoonPhasePacket.TYPE,
                RequestMoonPhasePacket.CODEC,
                (packet, ctx) -> ctx.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) ctx.player();
                    ServerLevel overworld = Objects.requireNonNull(player.level().getServer()).getLevel(Level.OVERWORLD);
                    int moonPhase = overworld != null ? (int) (overworld.getDayTime() / 24000L % 8L) : 0;
                    PacketDistributor.sendToPlayer(player, new MoonPhasePacket(moonPhase));
                })
        );

        if (FMLEnvironment.dist == Dist.CLIENT) {
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
            registrar.playToClient(
                    UserContentPacket.TYPE,
                    UserContentPacket.CODEC,
                    (packet, ctx) -> ctx.enqueueWork(() ->
                            mt.client.network.ClientPacketHandlers.handleUserContent(packet)
                    )
            );
            registrar.playToClient(
                    SyncAchievementsPacket.TYPE,
                    SyncAchievementsPacket.CODEC,
                    (packet, ctx) -> ctx.enqueueWork(() ->
                            mt.client.network.ClientPacketHandlers.handleSyncAchievements(packet)
                    )
            );
            registrar.playToClient(
                    SyncConfigPacket.TYPE,
                    SyncConfigPacket.CODEC,
                    (packet, ctx) -> ctx.enqueueWork(() ->
                            mt.client.network.ClientPacketHandlers.handleSyncConfig(packet)
                    )
            );
            registrar.playToClient(
                    MoonPhasePacket.TYPE,
                    MoonPhasePacket.CODEC,
                    (packet, ctx) -> ctx.enqueueWork(() ->
                            mt.client.network.ClientPacketHandlers.handleMoonPhase(packet)
                    )
            );
        } else {
            registrar.playToClient(DailySummaryPacket.TYPE, DailySummaryPacket.CODEC, (packet, ctx) -> {});
            registrar.playToClient(SleepingPlayersPacket.TYPE, SleepingPlayersPacket.CODEC, (packet, ctx) -> {});
            registrar.playToClient(WellRestedPacket.TYPE, WellRestedPacket.CODEC, (packet, ctx) -> {});
            registrar.playToClient(UserContentPacket.TYPE, UserContentPacket.CODEC, (packet, ctx) -> {});
            registrar.playToClient(SyncAchievementsPacket.TYPE, SyncAchievementsPacket.CODEC, (packet, ctx) -> {});
            registrar.playToClient(SyncConfigPacket.TYPE, SyncConfigPacket.CODEC, (packet, ctx) -> {});
            registrar.playToClient(MoonPhasePacket.TYPE, MoonPhasePacket.CODEC, (packet, ctx) -> {});
        }
    }

    public static void sendDailySummary(ServerPlayer player, DailySummaryPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendWellRested(ServerPlayer player, WellRestedPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendSleepingPlayers(ServerPlayer player, SleepingPlayersPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendUserContent(ServerPlayer player, UserContentPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendAchievements(ServerPlayer player, SyncAchievementsPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }

    public static void sendConfig(ServerPlayer player, SyncConfigPacket packet) {
        PacketDistributor.sendToPlayer(player, packet);
    }
}