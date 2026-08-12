package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.RequestMoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    private NetworkHandler() {}

    @SuppressWarnings("resource")
    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(
                RequestMoonPhasePacket.TYPE,
                RequestMoonPhasePacket.CODEC,
                (_, ctx) -> ctx.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) ctx.player();
                    ServerLevel overworld = player.level().getServer().getLevel(Level.OVERWORLD);
                    int moonPhase = overworld != null
                            ? (int) (overworld.getOverworldClockTime() / 24000L % 8L)
                            : 0;
                    PacketDistributor.sendToPlayer(player, new MoonPhasePacket(moonPhase));
                })
        );

        registrar.playToClient(DailySummaryPacket.TYPE, DailySummaryPacket.CODEC);
        registrar.playToClient(SleepingPlayersPacket.TYPE, SleepingPlayersPacket.CODEC);
        registrar.playToClient(WellRestedPacket.TYPE, WellRestedPacket.CODEC);
        registrar.playToClient(UserContentPacket.TYPE, UserContentPacket.CODEC);
        registrar.playToClient(SyncAchievementsPacket.TYPE, SyncAchievementsPacket.CODEC);
        registrar.playToClient(SyncConfigPacket.TYPE, SyncConfigPacket.CODEC);
        registrar.playToClient(MoonPhasePacket.TYPE, MoonPhasePacket.CODEC);
    }
}
