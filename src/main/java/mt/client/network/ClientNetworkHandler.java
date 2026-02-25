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

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientNetworkHandler {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        NetworkHandler.CHANNEL.messageBuilder(DailySummaryPacket.class)
                .codec(DailySummaryPacket.CODEC)
                .consumerMainThread((packet, ctx) -> {
                    Minecraft.getInstance().setScreen(new DailySummaryScreen(packet.summaries()));
                    ctx.setPacketHandled(true);
                })
                .add();

        NetworkHandler.CHANNEL.messageBuilder(SleepingPlayersPacket.class)
                .codec(SleepingPlayersPacket.CODEC)
                .consumerMainThread((packet, ctx) -> {
                    SleepingPlayersHud.updateSleepingCount(packet.sleepingCount(), packet.totalPlayers());
                    ctx.setPacketHandled(true);
                })
                .add();
    }

    public static void sendSummaryAcknowledge() {
        NetworkHandler.CHANNEL.send(new SummaryAcknowledgePacket(), net.minecraftforge.network.PacketDistributor.SERVER.noArg());
    }
}