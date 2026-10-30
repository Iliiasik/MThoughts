package mt.network;

import mt.MTConstants;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "5";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            MTConstants.id("main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private static int nextId;

    private NetworkHandler() {}

    public static void registerPackets() {
        CHANNEL.registerMessage(nextId++, RequestMoonPhasePacket.class, RequestMoonPhasePacket::write,
                buf -> new RequestMoonPhasePacket(), (packet, ctx) -> {
                    ServerPlayer player = ctx.get().getSender();
                    if (player != null) ctx.get().enqueueWork(() -> MTServer.sendMoonPhase(player));
                    ctx.get().setPacketHandled(true);
                });

        if (FMLEnvironment.dist.isClient()) {
            toClient(DailySummaryPacket.class, DailySummaryPacket::read, ClientPacketHandlers::handleDailySummary);
            toClient(SleepingPlayersPacket.class, SleepingPlayersPacket::read, ClientPacketHandlers::handleSleepingPlayers);
            toClient(WellRestedPacket.class, WellRestedPacket::read, ClientPacketHandlers::handleWellRested);
            toClient(UserContentPacket.class, UserContentPacket::read, ClientPacketHandlers::handleUserContent);
            toClient(SyncAchievementsPacket.class, SyncAchievementsPacket::read, ClientPacketHandlers::handleSyncAchievements);
            toClient(SyncConfigPacket.class, SyncConfigPacket::read, ClientPacketHandlers::handleSyncConfig);
            toClient(MoonPhasePacket.class, MoonPhasePacket::read, ClientPacketHandlers::handleMoonPhase);
        } else {
            toClient(DailySummaryPacket.class, DailySummaryPacket::read, packet -> {});
            toClient(SleepingPlayersPacket.class, SleepingPlayersPacket::read, packet -> {});
            toClient(WellRestedPacket.class, WellRestedPacket::read, packet -> {});
            toClient(UserContentPacket.class, UserContentPacket::read, packet -> {});
            toClient(SyncAchievementsPacket.class, SyncAchievementsPacket::read, packet -> {});
            toClient(SyncConfigPacket.class, SyncConfigPacket::read, packet -> {});
            toClient(MoonPhasePacket.class, MoonPhasePacket::read, packet -> {});
        }
    }

    public static void sendToPlayer(ServerPlayer player, MTPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToServer(MTPacket packet) {
        CHANNEL.sendToServer(packet);
    }

    private static <T extends MTPacket> void toClient(Class<T> type, Function<FriendlyByteBuf, T> reader,
                                                       Consumer<T> handler) {
        CHANNEL.registerMessage(nextId++, type, MTPacket::write, reader, (T packet, Supplier<NetworkEvent.Context> ctx) -> {
            ctx.get().enqueueWork(() -> handler.accept(packet));
            ctx.get().setPacketHandled(true);
        });
    }
}
