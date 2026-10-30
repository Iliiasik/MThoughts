package mt.network.packet;

import mt.MTConstants;
import mt.network.MTPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

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
        String wellRestedHudPosition,
        boolean hideWellRestedHud,
        boolean hideSleepingPlayersHud,
        boolean enableStarDust,
        boolean showSlideProgress,
        boolean hideHudMessagesWhenSleeping,
        float overlayTextScale,
        float overlayImageScale,
        boolean enableSpaceObjects
) implements MTPacket {
    public static SyncConfigPacket of(mt.config.MidnightThoughtsConfig cfg) {
        mt.config.MidnightThoughtsConfig.SleepOverlaySettings o = cfg.getSleepOverlay();
        mt.config.MidnightThoughtsConfig.UISettings ui = cfg.getUi();
        return new SyncConfigPacket(
                o.minSlideDisplayTimeMs,
                o.maxSlideDisplayTimeMs,
                o.fadeInDurationMs,
                o.fadeOutDurationMs,
                o.overlayOpacity,
                o.textOpacity,
                o.imageOpacity,
                o.specialSlideChance,
                o.enableOverlay,
                o.enableImage,
                o.enableDailySummaryScreen,
                o.useFactsApi,
                o.userContentReplaces,
                o.hideChatWhenSleeping,
                ui.theme,
                ui.wellRestedHudPosition,
                ui.hideWellRestedHud,
                ui.hideSleepingPlayersHud,
                o.enableStarDust,
                o.showSlideProgress,
                o.hideHudMessagesWhenSleeping,
                o.textScale,
                o.imageScale,
                o.enableSpaceObjects
        );
    }

    public static final ResourceLocation ID = MTConstants.id("sync_config");

    public static SyncConfigPacket read(FriendlyByteBuf buf) {
        return new SyncConfigPacket(
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readVarInt(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readUtf(),
            buf.readUtf(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readBoolean(),
            buf.readFloat(),
            buf.readFloat(),
            buf.readBoolean()
    );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(minSlideDisplayTimeMs());
        buf.writeVarInt(maxSlideDisplayTimeMs());
        buf.writeVarInt(fadeInDurationMs());
        buf.writeVarInt(fadeOutDurationMs());
        buf.writeFloat(overlayOpacity());
        buf.writeFloat(textOpacity());
        buf.writeFloat(imageOpacity());
        buf.writeFloat(specialSlideChance());
        buf.writeBoolean(enableOverlay());
        buf.writeBoolean(enableImage());
        buf.writeBoolean(enableDailySummaryScreen());
        buf.writeBoolean(useFactsApi());
        buf.writeBoolean(userContentReplaces());
        buf.writeBoolean(hideChatWhenSleeping());
        buf.writeUtf(theme());
        buf.writeUtf(wellRestedHudPosition());
        buf.writeBoolean(hideWellRestedHud());
        buf.writeBoolean(hideSleepingPlayersHud());
        buf.writeBoolean(enableStarDust());
        buf.writeBoolean(showSlideProgress());
        buf.writeBoolean(hideHudMessagesWhenSleeping());
        buf.writeFloat(overlayTextScale());
        buf.writeFloat(overlayImageScale());
        buf.writeBoolean(enableSpaceObjects());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
