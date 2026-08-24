package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.RequestMoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class NetworkHandler {

    private static MinecraftServer server;

    public static void setServer(MinecraftServer instance) {
        server = instance;
    }

    @SuppressWarnings("resource")
    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(DailySummaryPacket.TYPE, DailySummaryPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SleepingPlayersPacket.TYPE, SleepingPlayersPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(WellRestedPacket.TYPE, WellRestedPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UserContentPacket.TYPE, UserContentPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncAchievementsPacket.TYPE, SyncAchievementsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncConfigPacket.TYPE, SyncConfigPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MoonPhasePacket.TYPE, MoonPhasePacket.CODEC);

        PayloadTypeRegistry.playC2S().register(RequestMoonPhasePacket.TYPE, RequestMoonPhasePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestMoonPhasePacket.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            ServerLevel overworld = player.level().getServer().getLevel(Level.OVERWORLD);
            int moonPhase = overworld != null ? (int) (overworld.getDayTime() / 24000L % 8L) : 0;
            sendMoonPhase(player, new MoonPhasePacket(moonPhase));
        });
    }

    public static void sendDailySummary(ServerPlayer player, DailySummaryPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendWellRested(ServerPlayer player, WellRestedPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendSleepingPlayers(ServerPlayer player, SleepingPlayersPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendUserContent(ServerPlayer player, UserContentPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendAchievements(ServerPlayer player, SyncAchievementsPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendConfig(ServerPlayer player, SyncConfigPacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendMoonPhase(ServerPlayer player, MoonPhasePacket packet) {
        ServerPlayNetworking.send(player, packet);
    }

    public static void sendSleepingPlayersToAll(SleepingPlayersPacket packet) {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendSleepingPlayers(player, packet);
        }
    }
}
