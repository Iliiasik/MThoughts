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
import net.minecraft.client.MinecraftClient;

public class ClientNetworkHandler {

    public static void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(
                WellRestedPacket.ID,
                (packet, context) -> context.client().execute(() ->
                        WellRestedClientState.update(
                                packet.active(), packet.level(), packet.ticksRemaining(),
                                packet.totalTicks(), packet.nightmareMode(), packet.mvp()
                        )
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                DailySummaryPacket.ID,
                (packet, context) -> context.client().execute(() -> {
                    if (ServerConfigCache.has() ? ServerConfigCache.get().enableDailySummaryScreen()
                            : mt.config.MidnightThoughtsConfig.getInstance().isEnableDailySummaryScreen()) {
                        MinecraftClient.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
                    } else {
                        sendSummaryAcknowledge();
                    }
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SleepingPlayersPacket.ID,
                (packet, context) -> context.client().execute(() ->
                        SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers())
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                UserContentPacket.ID,
                (packet, context) -> context.client().execute(() -> {
                    MidnightThoughtsClient client = MidnightThoughtsClient.getInstance();
                    if (client != null) {
                        client.getUserContentLoader().applyServerContent(packet.content());
                    }
                })
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SyncAchievementsPacket.ID,
                (packet, context) -> context.client().execute(() ->
                        ClientAchievementCache.apply(packet.achievements())
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                SyncConfigPacket.ID,
                (packet, context) -> context.client().execute(() ->
                        ServerConfigCache.apply(packet)
                )
        );

        ClientPlayNetworking.registerGlobalReceiver(
                MoonPhasePacket.ID,
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