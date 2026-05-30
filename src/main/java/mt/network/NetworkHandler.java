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
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.Objects;

public class NetworkHandler {

    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(
                SummaryAcknowledgePacket.ID,
                (server, player, handler, buf, responseSender) -> {}
        );
        ServerPlayNetworking.registerGlobalReceiver(
                RequestMoonPhasePacket.ID,
                (server, player, handler, buf, responseSender) -> {
                    ServerWorld overworld = Objects.requireNonNull(player.getServer()).getWorld(World.OVERWORLD);
                    int moonPhase = overworld != null ? (int) (overworld.getTimeOfDay() / 24000L % 8L) : 0;
                    sendMoonPhase(player, new MoonPhasePacket(moonPhase));
                }
        );
    }

    public static void sendDailySummary(ServerPlayerEntity player, DailySummaryPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        DailySummaryPacket.encode(packet, buf);
        assert DailySummaryPacket.ID != null;
        ServerPlayNetworking.send(player, DailySummaryPacket.ID, buf);
    }

    public static void sendWellRested(ServerPlayerEntity player, WellRestedPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        WellRestedPacket.encode(packet, buf);
        assert WellRestedPacket.ID != null;
        ServerPlayNetworking.send(player, WellRestedPacket.ID, buf);
    }

    public static void sendSleepingPlayers(ServerPlayerEntity player, SleepingPlayersPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        SleepingPlayersPacket.encode(packet, buf);
        ServerPlayNetworking.send(player, SleepingPlayersPacket.ID, buf);
    }

    public static void sendUserContent(ServerPlayerEntity player, UserContentPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        UserContentPacket.encode(packet, buf);
        ServerPlayNetworking.send(player, UserContentPacket.ID, buf);
    }

    public static void sendAchievements(ServerPlayerEntity player, SyncAchievementsPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        SyncAchievementsPacket.encode(packet, buf);
        ServerPlayNetworking.send(player, SyncAchievementsPacket.ID, buf);
    }

    public static void sendConfig(ServerPlayerEntity player, SyncConfigPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        SyncConfigPacket.encode(packet, buf);
        ServerPlayNetworking.send(player, SyncConfigPacket.ID, buf);
    }

    public static void sendMoonPhase(ServerPlayerEntity player, MoonPhasePacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        MoonPhasePacket.encode(packet, buf);
        ServerPlayNetworking.send(player, MoonPhasePacket.ID, buf);
    }
}