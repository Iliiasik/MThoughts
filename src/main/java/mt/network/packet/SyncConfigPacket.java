package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;

public record SyncConfigPacket(int minSlideDisplayTimeMs, int maxSlideDisplayTimeMs, int fadeInDurationMs,
                               int fadeOutDurationMs, float overlayOpacity, float textOpacity, float imageOpacity,
                               float specialSlideChance, boolean enableOverlay, boolean enableImage,
                               boolean enableDailySummaryScreen, boolean useFactsApi, boolean userContentReplaces,
                               boolean hideChatWhenSleeping, String theme, String wellRestedHudPosition,
                               boolean hideWellRestedHud,
                               boolean hideSleepingPlayersHud, boolean hideThemeSwitchButton,
                               boolean enableStarDust, boolean showSlideProgress) {

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
                ui.hideThemeSwitchButton,
                o.enableStarDust,
                o.showSlideProgress
        );
    }

    public static void encode(SyncConfigPacket packet, FriendlyByteBuf buf) {
        buf.writeInt(packet.minSlideDisplayTimeMs);
        buf.writeInt(packet.maxSlideDisplayTimeMs);
        buf.writeInt(packet.fadeInDurationMs);
        buf.writeInt(packet.fadeOutDurationMs);
        buf.writeFloat(packet.overlayOpacity);
        buf.writeFloat(packet.textOpacity);
        buf.writeFloat(packet.imageOpacity);
        buf.writeFloat(packet.specialSlideChance);
        buf.writeBoolean(packet.enableOverlay);
        buf.writeBoolean(packet.enableImage);
        buf.writeBoolean(packet.enableDailySummaryScreen);
        buf.writeBoolean(packet.useFactsApi);
        buf.writeBoolean(packet.userContentReplaces);
        buf.writeBoolean(packet.hideChatWhenSleeping);
        buf.writeUtf(packet.theme);
        buf.writeUtf(packet.wellRestedHudPosition);
        buf.writeBoolean(packet.hideWellRestedHud);
        buf.writeBoolean(packet.hideSleepingPlayersHud);
        buf.writeBoolean(packet.hideThemeSwitchButton);
        buf.writeBoolean(packet.enableStarDust);
        buf.writeBoolean(packet.showSlideProgress);
    }

    public static SyncConfigPacket decode(FriendlyByteBuf buf) {
        return new SyncConfigPacket(
                buf.readInt(), buf.readInt(), buf.readInt(), buf.readInt(),
                buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(),
                buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean(),
                buf.readUtf(), buf.readUtf(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(),
                buf.readBoolean(), buf.readBoolean()
        );
    }
}
