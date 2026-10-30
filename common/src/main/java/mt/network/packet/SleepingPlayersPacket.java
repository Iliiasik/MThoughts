package mt.network.packet;

import mt.MTConstants;
import mt.network.MTPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SleepingPlayersPacket(int sleepingCount, int totalPlayers) implements MTPacket {
    public static final ResourceLocation ID = MTConstants.id("sleeping_players");

    public static SleepingPlayersPacket read(FriendlyByteBuf buf) {
        return new SleepingPlayersPacket(buf.readInt(), buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(sleepingCount());
        buf.writeInt(totalPlayers());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}