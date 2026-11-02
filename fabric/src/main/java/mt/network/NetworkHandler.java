package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.RequestMoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.PlayerSleepStatePacket;
import mt.server.MTServer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class NetworkHandler {

    private NetworkHandler() {}

    public static void registerPackets() {
        PayloadTypeRegistry.clientboundPlay().register(DailySummaryPacket.TYPE, DailySummaryPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SleepingPlayersPacket.TYPE, SleepingPlayersPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerSleepStatePacket.TYPE, PlayerSleepStatePacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UserContentPacket.TYPE, UserContentPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncAchievementsPacket.TYPE, SyncAchievementsPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SyncConfigPacket.TYPE, SyncConfigPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(MoonPhasePacket.TYPE, MoonPhasePacket.CODEC);

        PayloadTypeRegistry.serverboundPlay().register(RequestMoonPhasePacket.TYPE, RequestMoonPhasePacket.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestMoonPhasePacket.TYPE,
                (_, context) -> MTServer.sendMoonPhase(context.player()));
    }
}
