package mt.platform;

import mt.network.MTPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class MTNetwork {

    public interface Bridge {
        void sendToPlayer(ServerPlayer player, MTPacket packet);
    }

    private static Bridge bridge = (player, packet) -> {};

    private MTNetwork() {}

    public static void setBridge(Bridge value) {
        bridge = value;
    }

    public static void sendToPlayer(ServerPlayer player, MTPacket packet) {
        bridge.sendToPlayer(player, packet);
    }

    public static void sendToAll(MinecraftServer server, MTPacket packet) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            bridge.sendToPlayer(player, packet);
        }
    }
}
