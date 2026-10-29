package mt.platform;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class MTNetwork {

    public interface Bridge {
        void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
    }

    private static Bridge bridge = (player, payload) -> {};

    private MTNetwork() {}

    public static void setBridge(Bridge value) {
        bridge = value;
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        bridge.sendToPlayer(player, payload);
    }

    public static void sendToAll(MinecraftServer server, CustomPacketPayload payload) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            bridge.sendToPlayer(player, payload);
        }
    }
}
