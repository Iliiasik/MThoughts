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
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;

public class ClientNetworkHandler {

    public static void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(
                WellRestedPacket.ID,
                (client, handler, buf, responseSender) -> {
                    WellRestedPacket packet = WellRestedPacket.decode(buf);
                    client.execute(() ->
                            WellRestedClientState.update(
                                    packet.active(), packet.level(), packet.ticksRemaining(),
                                    packet.totalTicks(), packet.phase(), packet.nightmareMode(), packet.mvp()
                            )
                    );
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                DailySummaryPacket.ID,
                (client, handler, buf, responseSender) -> {
                    DailySummaryPacket packet = DailySummaryPacket.decode(buf);
                    client.execute(() -> {
                        if (MidnightThoughtsConfig.getInstance().isEnableDailySummaryScreen()) {
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
    }

    public static void sendSummaryAcknowledge() {
        ClientPlayNetworking.send(SummaryAcknowledgePacket.ID, PacketByteBufs.create());
    }
}