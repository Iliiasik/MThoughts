package mt.network.packet;

import mt.MTConstants;
import mt.network.MTPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record PlayerSleepStatePacket(boolean active, int level, int ticksRemaining, int totalTicks, boolean nightmareMode, boolean mvp) implements MTPacket {
    public static final ResourceLocation ID = MTConstants.id("player_sleep_state");

    public static PlayerSleepStatePacket read(FriendlyByteBuf buf) {
        return new PlayerSleepStatePacket(
            buf.readBoolean(),
            buf.readInt(),
            buf.readInt(),
            buf.readInt(),
            buf.readBoolean(),
            buf.readBoolean()
    );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(active());
        buf.writeInt(level());
        buf.writeInt(ticksRemaining());
        buf.writeInt(totalTicks());
        buf.writeBoolean(nightmareMode());
        buf.writeBoolean(mvp());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
