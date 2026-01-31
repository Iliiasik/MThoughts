package mt.client.network;

import mt.client.ui.DailySummaryScreen;
import mt.client.ui.SleepingPlayersHud;
import mt.network.NetworkHandler;
import mt.network.packet.DailySummaryPacket;
import mt.network.packet.SleepingPlayersPacket;
import mt.network.packet.SummaryAcknowledgePacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientNetworkHandler {
    private static int packetId = 0;

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        registerPacketHandlers();
    }

    public static void registerPacketHandlers() {
        NetworkHandler.CHANNEL.registerMessage(
                packetId++,
                DailySummaryPacket.class,
                DailySummaryPacket::encode,
                DailySummaryPacket::decode,
                ClientNetworkHandler::handleDailySummaryPacket,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        NetworkHandler.CHANNEL.registerMessage(
                packetId++,
                SleepingPlayersPacket.class,
                SleepingPlayersPacket::encode,
                SleepingPlayersPacket::decode,
                ClientNetworkHandler::handleSleepingPlayersPacket,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
        NetworkHandler.CHANNEL.registerMessage(
                packetId++,
                SummaryAcknowledgePacket.class,
                SummaryAcknowledgePacket::encode,
                SummaryAcknowledgePacket::decode,
                (msg, ctx) -> {},
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    private static void handleDailySummaryPacket(DailySummaryPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Minecraft.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
        });
        ctx.get().setPacketHandled(true);
    }

    private static void handleSleepingPlayersPacket(SleepingPlayersPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers());
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sendSummaryAcknowledge() {
        NetworkHandler.CHANNEL.sendToServer(new SummaryAcknowledgePacket());
    }
}
