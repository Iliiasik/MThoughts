package mt.client.network;

import mt.client.manager.WellRestedClientState;
import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.config.MidnightThoughtsConfig;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
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
                    if (MidnightThoughtsConfig.getInstance().isEnableDailySummaryScreen()) {
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
    }

    public static void sendSummaryAcknowledge() {
        ClientPlayNetworking.send(new SummaryAcknowledgePacket());
    }
}