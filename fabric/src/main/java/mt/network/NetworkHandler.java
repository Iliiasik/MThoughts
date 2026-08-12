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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class NetworkHandler {

    private NetworkHandler() {}

    @SuppressWarnings("resource")
    public static void registerPackets() {
        PayloadTypeRegistry.clientboundPlay().register(DailySummaryPacket.TYPE, DailySummaryPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SleepingPlayersPacket.TYPE, SleepingPlayersPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(WellRestedPacket.TYPE, WellRestedPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UserContentPacket.TYPE, UserContentPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncAchievementsPacket.TYPE, SyncAchievementsPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncConfigPacket.TYPE, SyncConfigPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MoonPhasePacket.TYPE, MoonPhasePacket.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(RequestMoonPhasePacket.TYPE, RequestMoonPhasePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestMoonPhasePacket.TYPE, (_, context) -> {
            ServerPlayer player = context.player();
            ServerLevel overworld = player.level().getServer().getLevel(Level.OVERWORLD);
            int moonPhase = overworld != null ? (int) (overworld.getOverworldClockTime() / 24000L % 8L) : 0;
            ServerPlayNetworking.send(player, new MoonPhasePacket(moonPhase));
        });
    }
}
