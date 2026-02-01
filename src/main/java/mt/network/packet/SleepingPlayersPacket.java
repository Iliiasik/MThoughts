package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SleepingPlayersPacket {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("midnightthoughts", "sleeping_players");

    private final int sleepingCount;
    private final int totalPlayers;

    public SleepingPlayersPacket(int sleepingCount, int totalPlayers) {
        this.sleepingCount = sleepingCount;
        this.totalPlayers = totalPlayers;
    }

    public int sleepingCount() {
        return sleepingCount;
    }

    public int totalPlayers() {
        return totalPlayers;
    }

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