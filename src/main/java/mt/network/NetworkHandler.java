package mt.network;

import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    public static void registerPackets() {
        LOGGER.info("[NetworkHandler] Registering packets...");

        PayloadTypeRegistry.playS2C().register(
            DailySummaryPacket.ID,
            DailySummaryPacket.CODEC
        );

        PayloadTypeRegistry.playS2C().register(
            SleepingPlayersPacket.ID,
            SleepingPlayersPacket.CODEC
        );

        PayloadTypeRegistry.playC2S().register(
            SummaryAcknowledgePacket.ID,
            SummaryAcknowledgePacket.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
            SummaryAcknowledgePacket.ID,
            (packet, context) -> LOGGER.debug("[NetworkHandler] Player {} acknowledged summary",
                context.player().getGameProfile().getName())
        );

        LOGGER.info("[NetworkHandler] Network packets registered successfully");
    }

    public static void sendToClient(ServerPlayerEntity player, CustomPayload payload) {
        LOGGER.debug("[NetworkHandler] Sending {} to player {}", payload.getClass().getSimpleName(), player.getGameProfile().getName());
        ServerPlayNetworking.send(player, payload);
    }
}
