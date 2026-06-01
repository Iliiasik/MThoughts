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
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.Objects;

public class NetworkHandler {

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(DailySummaryPacket.ID, DailySummaryPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SleepingPlayersPacket.ID, SleepingPlayersPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(WellRestedPacket.ID, WellRestedPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UserContentPacket.ID, UserContentPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncAchievementsPacket.ID, SyncAchievementsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncConfigPacket.ID, SyncConfigPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MoonPhasePacket.ID, MoonPhasePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(SummaryAcknowledgePacket.ID, SummaryAcknowledgePacket.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestMoonPhasePacket.ID, RequestMoonPhasePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SummaryAcknowledgePacket.ID, (packet, context) -> {});
        ServerPlayNetworking.registerGlobalReceiver(RequestMoonPhasePacket.ID, (packet, context) -> {
            ServerPlayerEntity player = context.player();
            ServerWorld overworld = Objects.requireNonNull(player.getEntityWorld().getServer()).getWorld(World.OVERWORLD);
            int moonPhase = overworld != null ? (int) (overworld.getTimeOfDay() / 24000L % 8L) : 0;
            ServerPlayNetworking.send(player, new MoonPhasePacket(moonPhase));
        });
    }

    public static void sendDailySummary(ServerPlayerEntity player, DailySummaryPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendWellRested(ServerPlayerEntity player, WellRestedPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendSleepingPlayers(ServerPlayerEntity player, SleepingPlayersPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendUserContent(ServerPlayerEntity player, UserContentPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendAchievements(ServerPlayerEntity player, SyncAchievementsPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendConfig(ServerPlayerEntity player, SyncConfigPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }
}