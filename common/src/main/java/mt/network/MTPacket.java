package mt.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface MTPacket {
    ResourceLocation id();

    void write(FriendlyByteBuf buf);
}
