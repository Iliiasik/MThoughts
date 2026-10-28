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

    public static void registerPackets() {
        PayloadTypeRegistry.playS2C().register(DailySummaryPacket.TYPE, DailySummaryPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SleepingPlayersPacket.TYPE, SleepingPlayersPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(WellRestedPacket.TYPE, WellRestedPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(UserContentPacket.TYPE, UserContentPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncAchievementsPacket.TYPE, SyncAchievementsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SyncConfigPacket.TYPE, SyncConfigPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(MoonPhasePacket.TYPE, MoonPhasePacket.CODEC);

        PayloadTypeRegistry.playC2S().register(RequestMoonPhasePacket.TYPE, RequestMoonPhasePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestMoonPhasePacket.TYPE, (packet, context) -> {
            ServerPlayer player = context.player();
            ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
            int moonPhase = overworld != null ? (int) (overworld.getDayTime() / 24000L % 8L) : 0;
            ServerPlayNetworking.send(player, new MoonPhasePacket(moonPhase));
        });
    }
}
