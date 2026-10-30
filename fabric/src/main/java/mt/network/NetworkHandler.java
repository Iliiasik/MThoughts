package mt.network;

import mt.network.packet.RequestMoonPhasePacket;
import mt.server.MTServer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class NetworkHandler {

    private NetworkHandler() {}

    public static void registerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(RequestMoonPhasePacket.ID,
                (server, player, handler, buf, responseSender) -> server.execute(() -> MTServer.sendMoonPhase(player)));
    }

    public static void sendToPlayer(ServerPlayer player, MTPacket packet) {
        ServerPlayNetworking.send(player, packet.id(), encode(packet));
    }

    public static FriendlyByteBuf encode(MTPacket packet) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        packet.write(buf);
        return buf;
    }
}
