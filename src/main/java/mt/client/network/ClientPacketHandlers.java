package mt.client.network;

import mt.server.config.MidnightThoughtsConfig;
import mt.client.manager.WellRestedClientState;
import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.network.NetworkHandler;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import mt.network.packet.WellRestedPacket;
import net.minecraft.client.Minecraft;

public class ClientPacketHandlers {

    public static void handleDailySummary(DailySummaryPacket packet) {
        if (MidnightThoughtsConfig.getInstance().isEnableDailySummaryScreen()) {
            Minecraft.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
        } else {
            sendSummaryAcknowledge();
        }
    }

    public static void handleSleepingPlayers(SleepingPlayersPacket packet) {
        SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers());
    }

    public static void handleWellRested(WellRestedPacket packet) {
        WellRestedClientState.update(
                packet.active(), packet.level(), packet.ticksRemaining(),
                packet.totalTicks(), packet.phase(), packet.nightmareMode(), packet.mvp()
        );
    }

    public static void sendSummaryAcknowledge() {
        NetworkHandler.CHANNEL.sendToServer(new SummaryAcknowledgePacket());
    }
}