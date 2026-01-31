package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SummaryAcknowledgePacket {
    public static final ResourceLocation ID = new ResourceLocation("midnightthoughts", "summary_acknowledge");

    public static void encode(SummaryAcknowledgePacket packet, FriendlyByteBuf buf) {
    }

    public static SummaryAcknowledgePacket decode(FriendlyByteBuf buf) {
        return new SummaryAcknowledgePacket();
    }
}