package mt.network;

import mt.client.network.ClientPacketHandlers;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.RequestMoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import mt.server.MTServer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "5";

    private NetworkHandler() {}

    public static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(RequestMoonPhasePacket.TYPE, RequestMoonPhasePacket.CODEC,
                (packet, ctx) -> ctx.enqueueWork(() -> MTServer.sendMoonPhase((ServerPlayer) ctx.player())));

        if (FMLEnvironment.dist.isClient()) {
            toClient(registrar, DailySummaryPacket.TYPE, DailySummaryPacket.CODEC, ClientPacketHandlers::handleDailySummary);
            toClient(registrar, SleepingPlayersPacket.TYPE, SleepingPlayersPacket.CODEC, ClientPacketHandlers::handleSleepingPlayers);
            toClient(registrar, WellRestedPacket.TYPE, WellRestedPacket.CODEC, ClientPacketHandlers::handleWellRested);
            toClient(registrar, UserContentPacket.TYPE, UserContentPacket.CODEC, ClientPacketHandlers::handleUserContent);
            toClient(registrar, SyncAchievementsPacket.TYPE, SyncAchievementsPacket.CODEC, ClientPacketHandlers::handleSyncAchievements);
            toClient(registrar, SyncConfigPacket.TYPE, SyncConfigPacket.CODEC, ClientPacketHandlers::handleSyncConfig);
            toClient(registrar, MoonPhasePacket.TYPE, MoonPhasePacket.CODEC, ClientPacketHandlers::handleMoonPhase);
        } else {
            toClient(registrar, DailySummaryPacket.TYPE, DailySummaryPacket.CODEC, packet -> {});
            toClient(registrar, SleepingPlayersPacket.TYPE, SleepingPlayersPacket.CODEC, packet -> {});
            toClient(registrar, WellRestedPacket.TYPE, WellRestedPacket.CODEC, packet -> {});
            toClient(registrar, UserContentPacket.TYPE, UserContentPacket.CODEC, packet -> {});
            toClient(registrar, SyncAchievementsPacket.TYPE, SyncAchievementsPacket.CODEC, packet -> {});
            toClient(registrar, SyncConfigPacket.TYPE, SyncConfigPacket.CODEC, packet -> {});
            toClient(registrar, MoonPhasePacket.TYPE, MoonPhasePacket.CODEC, packet -> {});
        }
    }

    private static <T extends CustomPacketPayload> void toClient(PayloadRegistrar registrar,
                                                                CustomPacketPayload.Type<T> type,
                                                                StreamCodec<? super FriendlyByteBuf, T> codec,
                                                                Consumer<T> handler) {
        registrar.playToClient(type, codec, (packet, ctx) -> ctx.enqueueWork(() -> handler.accept(packet)));
    }
}
