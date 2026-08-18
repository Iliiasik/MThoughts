package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.function.BiConsumer;

public class NetworkHandler {
    private static final String NAMESPACE = "midnightthoughts";

    public static final ResourceLocation DAILY_SUMMARY = id("daily_summary");
    public static final ResourceLocation WELL_RESTED = id("well_rested");
    public static final ResourceLocation SLEEPING_PLAYERS = id("sleeping_players");
    public static final ResourceLocation USER_CONTENT = id("user_content");
    public static final ResourceLocation SYNC_ACHIEVEMENTS = id("sync_achievements");
    public static final ResourceLocation SYNC_CONFIG = id("sync_config");
    public static final ResourceLocation MOON_PHASE = id("moon_phase");
    public static final ResourceLocation REQUEST_MOON_PHASE = id("request_moon_phase");

    private static MinecraftServer server;

    public static void setServer(MinecraftServer instance) {
        server = instance;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(NAMESPACE, path);
    }

    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(REQUEST_MOON_PHASE,
                (server, player, handler, buf, responseSender) -> server.execute(() -> {
                    ServerLevel overworld = Objects.requireNonNull(player.getServer()).getLevel(Level.OVERWORLD);
                    int moonPhase = overworld != null ? (int) (overworld.getDayTime() / 24000L % 8L) : 0;
                    sendMoonPhase(player, new MoonPhasePacket(moonPhase));
                }));
    }

    public static void sendDailySummary(ServerPlayer player, DailySummaryPacket packet) {
        send(player, DAILY_SUMMARY, packet, DailySummaryPacket::encode);
    }

    public static void sendWellRested(ServerPlayer player, WellRestedPacket packet) {
        send(player, WELL_RESTED, packet, WellRestedPacket::encode);
    }

    public static void sendSleepingPlayers(ServerPlayer player, SleepingPlayersPacket packet) {
        send(player, SLEEPING_PLAYERS, packet, SleepingPlayersPacket::encode);
    }

    public static void sendUserContent(ServerPlayer player, UserContentPacket packet) {
        send(player, USER_CONTENT, packet, UserContentPacket::encode);
    }

    public static void sendAchievements(ServerPlayer player, SyncAchievementsPacket packet) {
        send(player, SYNC_ACHIEVEMENTS, packet, SyncAchievementsPacket::encode);
    }

    public static void sendConfig(ServerPlayer player, SyncConfigPacket packet) {
        send(player, SYNC_CONFIG, packet, SyncConfigPacket::encode);
    }

    public static void sendMoonPhase(ServerPlayer player, MoonPhasePacket packet) {
        send(player, MOON_PHASE, packet, MoonPhasePacket::encode);
    }

    public static void sendSleepingPlayersToAll(SleepingPlayersPacket packet) {
        if (server == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sendSleepingPlayers(player, packet);
        }
    }

    private static <T> void send(ServerPlayer player, ResourceLocation channel, T packet,
                                 BiConsumer<T, FriendlyByteBuf> encoder) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        encoder.accept(packet, buf);
        ServerPlayNetworking.send(player, channel, buf);
    }
}
