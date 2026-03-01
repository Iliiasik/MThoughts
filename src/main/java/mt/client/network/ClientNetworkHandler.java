package mt.client.network;

import mt.network.packet.SummaryAcknowledgePacket;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientNetworkHandler {
    public static void sendSummaryAcknowledge() {
        PacketDistributor.sendToServer(new SummaryAcknowledgePacket());
    }
}