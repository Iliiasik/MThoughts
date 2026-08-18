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
import mt.network.packet.SyncAchievementsPacket;
import mt.network.packet.SyncConfigPacket;
import mt.network.packet.UserContentPacket;
import mt.network.packet.WellRestedPacket;
import net.minecraft.client.Minecraft;

public class ClientPacketHandlers {

    public static void handleDailySummary(DailySummaryPacket packet) {
        if (mt.config.MidnightThoughtsConfig.getInstance().isEnableDailySummaryScreen()) {
            Minecraft.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
        }
    }

    public static void handleSleepingPlayers(SleepingPlayersPacket packet) {
        SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers());
    }

    public static void handleWellRested(WellRestedPacket packet) {
        WellRestedClientState.update(
                packet.active(), packet.level(), packet.ticksRemaining(),
                packet.totalTicks(), packet.nightmareMode(), packet.mvp()
        );
    }

    public static void handleUserContent(UserContentPacket packet) {
        MidnightThoughtsClient client = MidnightThoughtsClient.getInstance();
        if (client != null) {
            client.getUserContentLoader().applyServerContent(packet.content());
        }
    }

    public static void handleSyncAchievements(SyncAchievementsPacket packet) {
        ClientAchievementCache.apply(packet.achievements());
    }

    public static void handleSyncConfig(SyncConfigPacket packet) {
        ServerConfigCache.apply(packet);
    }

    public static void handleMoonPhase(MoonPhasePacket packet) {
        MidnightThoughtsClient client = MidnightThoughtsClient.getInstance();
        if (client != null) {
            client.getOverlayRenderer().setMoonPhase(packet.moonPhase());
        }
    }

}