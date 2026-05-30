package mt.client.network;

import mt.cache.ClientAchievementCache;
import mt.cache.ServerConfigCache;
import mt.client.MidnightThoughtsClient;
import mt.client.manager.WellRestedClientState;
import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.MoonPhasePacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;

public class ClientNetworkHandler {

    public static void registerPacketHandlers() {
        assert WellRestedPacket.ID != null;
        ClientPlayNetworking.registerGlobalReceiver(
                WellRestedPacket.ID,
                (client, handler, buf, responseSender) -> {
                    WellRestedPacket packet = WellRestedPacket.decode(buf);
                    client.execute(() ->
                            WellRestedClientState.update(
                                    packet.active(), packet.level(), packet.ticksRemaining(),
                                    packet.totalTicks(), packet.nightmareMode(), packet.mvp()
                            )
                    );
                }
        );

        assert DailySummaryPacket.ID != null;
        ClientPlayNetworking.registerGlobalReceiver(
                DailySummaryPacket.ID,
                (client, handler, buf, responseSender) -> {
                    DailySummaryPacket packet = DailySummaryPacket.decode(buf);
                    client.execute(() -> {
                        if (ServerConfigCache.has() ? ServerConfigCache.get().enableDailySummaryScreen()
                                : mt.config.MidnightThoughtsConfig.getInstance().isEnableDailySummaryScreen()) {
                            MinecraftClient.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
                        } else {
                            sendSummaryAcknowledge();
                        }
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SleepingPlayersPacket.ID,
                (client, handler, buf, responseSender) -> {
                    SleepingPlayersPacket packet = SleepingPlayersPacket.decode(buf);
                    client.execute(() ->
                            SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers())
                    );
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                UserContentPacket.ID,
                (client, handler, buf, responseSender) -> {
                    UserContentPacket packet = UserContentPacket.decode(buf);
                    client.execute(() -> {
                        MidnightThoughtsClient instance = MidnightThoughtsClient.getInstance();
                        if (instance != null) {
                            instance.getUserContentLoader().applyServerContent(packet.content());
                        }
                    });
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SyncAchievementsPacket.ID,
                (client, handler, buf, responseSender) -> {
                    SyncAchievementsPacket packet = SyncAchievementsPacket.decode(buf);
                    client.execute(() ->
                            ClientAchievementCache.apply(packet.achievements())
                    );
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SyncConfigPacket.ID,
                (client, handler, buf, responseSender) -> {
                    SyncConfigPacket packet = SyncConfigPacket.decode(buf);
                    client.execute(() ->
                            ServerConfigCache.apply(packet)
                    );
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                MoonPhasePacket.ID,
                (client, handler, buf, responseSender) -> {
                    MoonPhasePacket packet = MoonPhasePacket.decode(buf);
                    client.execute(() -> {
                        MidnightThoughtsClient instance = MidnightThoughtsClient.getInstance();
                        if (instance != null) {
                            instance.getOverlayRenderer().setMoonPhase(packet.moonPhase());
                        }
                    });
                }
        );
    }

    public static void sendSummaryAcknowledge() {
        ClientPlayNetworking.send(SummaryAcknowledgePacket.ID, PacketByteBufs.create());
    }
}