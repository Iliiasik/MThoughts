package mt.network.packet;

import mt.MTConstants;
import mt.network.MTPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record RequestMoonPhasePacket() implements MTPacket {
    public static final ResourceLocation ID = MTConstants.id("request_moon_phase");

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}