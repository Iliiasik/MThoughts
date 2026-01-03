package mt.client.network;

import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

public class ClientNetworkHandler {

    public static void registerPacketHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(
            DailySummaryPacket.ID,
            (packet, context) -> {
                context.client().execute(() -> {
                    MinecraftClient.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
                });
            }
        );

        ClientPlayNetworking.registerGlobalReceiver(
            SleepingPlayersPacket.ID,
            (packet, context) -> {
                context.client().execute(() -> SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers()));
            }
        );
    }

    public static void sendSummaryAcknowledge() {
        ClientPlayNetworking.send(new SummaryAcknowledgePacket());
    }
}

