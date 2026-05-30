package mt.network.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record SyncConfigPacket(
        int minSlideDisplayTimeMs,
        int maxSlideDisplayTimeMs,
        int fadeInDurationMs,
        int fadeOutDurationMs,
        float overlayOpacity,
        float textOpacity,
        float imageOpacity,
        float specialSlideChance,
        boolean enableOverlay,
        boolean enableImage,
        boolean enableDailySummaryScreen,
        boolean useFactsApi,
        boolean userContentReplaces,
        boolean hideChatWhenSleeping,
        String theme,
        boolean hideWellRestedHud,
        boolean hideThemeSwitchButton
) {
    public static final Identifier ID = new Identifier("midnightthoughts", "sync_config");

    public static void encode(SyncConfigPacket p, PacketByteBuf buf) {
        buf.writeInt(p.minSlideDisplayTimeMs());
        buf.writeInt(p.maxSlideDisplayTimeMs());
        buf.writeInt(p.fadeInDurationMs());
        buf.writeInt(p.fadeOutDurationMs());
        buf.writeFloat(p.overlayOpacity());
        buf.writeFloat(p.textOpacity());
        buf.writeFloat(p.imageOpacity());
        buf.writeFloat(p.specialSlideChance());
        buf.writeBoolean(p.enableOverlay());
        buf.writeBoolean(p.enableImage());
        buf.writeBoolean(p.enableDailySummaryScreen());
        buf.writeBoolean(p.useFactsApi());
        buf.writeBoolean(p.userContentReplaces());
        buf.writeBoolean(p.hideChatWhenSleeping());
        buf.writeString(p.theme());
        buf.writeBoolean(p.hideWellRestedHud());
        buf.writeBoolean(p.hideThemeSwitchButton());
    }

    public static SyncConfigPacket decode(PacketByteBuf buf) {
        return new SyncConfigPacket(
                buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(),
                buf.readString(),
                buf.readBoolean(), buf.readBoolean()
        );
    }
}