package mt.client.network;

import mt.server.config.MidnightThoughtsConfig;
import mt.client.manager.WellRestedClientState;
import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import mt.network.packet.WellRestedPacket;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientPacketHandlers {

    public static void handleDailySummary(DailySummaryPacket packet) {
        if (MidnightThoughtsConfig.getInstance().isEnableDailySummaryScreen()) {
            Minecraft.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
        } else {
            PacketDistributor.sendToServer(new SummaryAcknowledgePacket());
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
        PacketDistributor.sendToServer(new SummaryAcknowledgePacket());
    }
}