package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;

public record SleepingPlayersPacket(int sleepingCount, int totalPlayers) {

    public static void encode(SleepingPlayersPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.sleepingCount);
        buf.writeInt(packet.totalPlayers);
    }

    public static SleepingPlayersPacket decode(FriendlyByteBuf buf) {
        int sleepingCount = buf.readInt();
        int totalPlayers = buf.readInt();
        return new SleepingPlayersPacket(sleepingCount, totalPlayers);
    }
}