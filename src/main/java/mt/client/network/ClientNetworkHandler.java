package mt.client.network;

import mt.network.NetworkHandler;
import mt.network.packet.SummaryAcknowledgePacket;
import net.minecraftforge.network.PacketDistributor;

public class ClientNetworkHandler {
    public static void sendSummaryAcknowledge() {
        NetworkHandler.CHANNEL.send(new SummaryAcknowledgePacket(), PacketDistributor.SERVER.noArg());
    }
}