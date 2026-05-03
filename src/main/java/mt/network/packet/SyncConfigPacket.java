package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;

public record SyncConfigPacket(int minSlideDisplayTimeMs, int maxSlideDisplayTimeMs, float overlayOpacity,
                               float specialSlideChance, boolean enableOverlay, boolean enableImage,
                               boolean enableDailySummaryScreen, boolean useFactsApi, boolean userContentReplaces,
                               boolean hideChatWhenSleeping, String theme, boolean hideWellRestedHud,
                               boolean hideThemeSwitchButton) {

    public static void encode(SyncConfigPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.minSlideDisplayTimeMs);
        buf.writeInt(packet.maxSlideDisplayTimeMs);
        buf.writeFloat(packet.overlayOpacity);
        buf.writeFloat(packet.specialSlideChance);
        buf.writeBoolean(packet.enableOverlay);
        buf.writeBoolean(packet.enableImage);
        buf.writeBoolean(packet.enableDailySummaryScreen);
        buf.writeBoolean(packet.useFactsApi);
        buf.writeBoolean(packet.userContentReplaces);
        buf.writeBoolean(packet.hideChatWhenSleeping);
        buf.writeUtf(packet.theme);
        buf.writeBoolean(packet.hideWellRestedHud);
        buf.writeBoolean(packet.hideThemeSwitchButton);
    }

    public static SyncConfigPacket decode(FriendlyByteBuf buf) {
        return new SyncConfigPacket(
                buf.readInt(), buf.readInt(),
                buf.readFloat(), buf.readFloat(),
                buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(),
                buf.readUtf(),
                buf.readBoolean(), buf.readBoolean()
        );
    }
}