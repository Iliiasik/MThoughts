package mt.client.network;

import mt.network.NetworkHandler;
import mt.network.packet.SummaryAcknowledgePacket;

public class ClientNetworkHandler {
    public static void sendSummaryAcknowledge() {
        NetworkHandler.sendToServer(new SummaryAcknowledgePacket());
    }
}