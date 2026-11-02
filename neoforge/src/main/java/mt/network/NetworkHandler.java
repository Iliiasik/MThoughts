package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.PlayerSleepStatePacket;
import mt.network.packet.RequestMoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.server.MTServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "5";

    private NetworkHandler() {}

    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(RequestMoonPhasePacket.TYPE, RequestMoonPhasePacket.CODEC,
                (_, ctx) -> ctx.enqueueWork(() -> MTServer.sendMoonPhase((ServerPlayer) ctx.player())));

        registrar.playToClient(DailySummaryPacket.TYPE, DailySummaryPacket.CODEC);
        registrar.playToClient(SleepingPlayersPacket.TYPE, SleepingPlayersPacket.CODEC);
        registrar.playToClient(PlayerSleepStatePacket.TYPE, PlayerSleepStatePacket.CODEC);
        registrar.playToClient(UserContentPacket.TYPE, UserContentPacket.CODEC);
        registrar.playToClient(SyncAchievementsPacket.TYPE, SyncAchievementsPacket.CODEC);
        registrar.playToClient(SyncConfigPacket.TYPE, SyncConfigPacket.CODEC);
        registrar.playToClient(MoonPhasePacket.TYPE, MoonPhasePacket.CODEC);
    }
}
