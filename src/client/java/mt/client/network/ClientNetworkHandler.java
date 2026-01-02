package mt.client.network;

import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientNetworkHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    public static void registerPacketHandlers() {
        LOGGER.info("[ClientNetworkHandler] Registering packet handlers...");

        ClientPlayNetworking.registerGlobalReceiver(
            DailySummaryPacket.ID,
            (packet, context) -> {
                LOGGER.info("[ClientNetworkHandler] Received daily summary packet with {} players", packet.summaries().size());
                context.client().execute(() -> {
                    LOGGER.info("[ClientNetworkHandler] Opening DailySummaryScreen");
                    MinecraftClient.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
                });
            }
        );

        ClientPlayNetworking.registerGlobalReceiver(
            SleepingPlayersPacket.ID,
            (packet, context) -> {
                LOGGER.info("[ClientNetworkHandler] Received sleeping players packet: {}/{}", packet.sleepingCount(), packet.totalPlayers());
                context.client().execute(() -> SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers()));
            }
        );

        LOGGER.info("[ClientNetworkHandler] Client network handlers registered successfully");
    }

    public static void sendSummaryAcknowledge() {
        LOGGER.info("[ClientNetworkHandler] Sending summary acknowledge");
        ClientPlayNetworking.send(new SummaryAcknowledgePacket());
    }
}

