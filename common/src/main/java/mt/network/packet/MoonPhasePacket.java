package mt.network.packet;

import mt.MTConstants;
import mt.network.MTPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public record MoonPhasePacket(int moonPhase) implements MTPacket {
    public static final ResourceLocation ID = MTConstants.id("moon_phase");

    public static MoonPhasePacket read(FriendlyByteBuf buf) {
        return new MoonPhasePacket(buf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(moonPhase());
    }

    public static MoonPhasePacket current(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        return new MoonPhasePacket(overworld != null ? overworld.getMoonPhase() : 0);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
