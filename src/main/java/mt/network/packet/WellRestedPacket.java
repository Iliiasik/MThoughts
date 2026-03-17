package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class WellRestedPacket {
    public static final Identifier ID = Identifier.of("midnightthoughts", "well_rested");

    private final boolean active;
    private final int level;
    private final int ticksRemaining;
    private final int totalTicks;
    private final int phase;
    private final boolean nightmareMode;
    private final boolean mvp;

    public WellRestedPacket(boolean active, int level, int ticksRemaining, int totalTicks,
                            int phase, boolean nightmareMode, boolean mvp) {
        this.active = active;
        this.level = level;
        this.ticksRemaining = ticksRemaining;
        this.totalTicks = totalTicks;
        this.phase = phase;
        this.nightmareMode = nightmareMode;
        this.mvp = mvp;
    }

    public boolean active() { return active; }
    public int level() { return level; }
    public int ticksRemaining() { return ticksRemaining; }
    public int totalTicks() { return totalTicks; }
    public int phase() { return phase; }
    public boolean nightmareMode() { return nightmareMode; }
    public boolean mvp() { return mvp; }

    public static void encode(WellRestedPacket packet, PacketByteBuf buf) {
        buf.writeBoolean(packet.active);
        buf.writeInt(packet.level);
        buf.writeInt(packet.ticksRemaining);
        buf.writeInt(packet.totalTicks);
        buf.writeInt(packet.phase);
        buf.writeBoolean(packet.nightmareMode);
        buf.writeBoolean(packet.mvp);
    }

    public static WellRestedPacket decode(PacketByteBuf buf) {
        return new WellRestedPacket(
                buf.readBoolean(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readInt(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }
}