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
import net.minecraft.client.Minecraft;

public class ClientNetworkHandler {

    public static void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(
                WellRestedPacket.TYPE,
                (packet, context) -> context.client().execute(() ->
                        WellRestedClientState.update(
                                packet.active(), packet.level(), packet.ticksRemaining(),
                                packet.totalTicks(), packet.nightmareMode(), packet.mvp()
                        )
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                DailySummaryPacket.TYPE,
                (packet, context) -> context.client().execute(() -> {
                    if (ServerConfigCache.has() ? ServerConfigCache.get().enableDailySummaryScreen()
                            : mt.config.MidnightThoughtsConfig.getInstance().isEnableDailySummaryScreen()) {
                        Minecraft.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
                    } else {
                        sendSummaryAcknowledge();
                    }
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SleepingPlayersPacket.TYPE,
                (packet, context) -> context.client().execute(() ->
                        SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers())
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                UserContentPacket.TYPE,
                (packet, context) -> context.client().execute(() -> {
                    MidnightThoughtsClient client = MidnightThoughtsClient.getInstance();
                    if (client != null) {
                        client.getUserContentLoader().applyServerContent(packet.content());
                    }
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SyncAchievementsPacket.TYPE,
                (packet, context) -> context.client().execute(() ->
                        ClientAchievementCache.apply(packet.achievements())
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SyncConfigPacket.TYPE,
                (packet, context) -> context.client().execute(() ->
                        ServerConfigCache.apply(packet)
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                MoonPhasePacket.TYPE,
                (packet, context) -> context.client().execute(() -> {
                    MidnightThoughtsClient client = MidnightThoughtsClient.getInstance();
                    if (client != null) {
                        client.getOverlayRenderer().setMoonPhase(packet.moonPhase());
                    }
                })
        );
    }

    public static void sendSummaryAcknowledge() {
        ClientPlayNetworking.send(new SummaryAcknowledgePacket());
    }
}