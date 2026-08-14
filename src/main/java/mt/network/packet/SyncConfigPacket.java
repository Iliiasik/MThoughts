package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

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
        boolean hideThemeSwitchButton,
        boolean enableStarDust,
        boolean showSlideProgress
) implements CustomPacketPayload {

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
    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "sync_config");
    public static final CustomPacketPayload.Type<@NotNull SyncConfigPacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);
    public static final StreamCodec<FriendlyByteBuf, SyncConfigPacket> CODEC = StreamCodec.of(
            (buf, p) -> {
                buf.writeVarInt(p.minSlideDisplayTimeMs());
                buf.writeVarInt(p.maxSlideDisplayTimeMs());
                buf.writeVarInt(p.fadeInDurationMs());
                buf.writeVarInt(p.fadeOutDurationMs());
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
                buf.writeUtf(p.theme());
                buf.writeUtf(p.wellRestedHudPosition());
                buf.writeBoolean(p.hideWellRestedHud());
                buf.writeBoolean(p.hideSleepingPlayersHud());
                buf.writeBoolean(p.hideThemeSwitchButton());
                buf.writeBoolean(p.enableStarDust());
                buf.writeBoolean(p.showSlideProgress());
            },
            buf -> new SyncConfigPacket(
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
                    buf.readBoolean()
            )
    );

    @Override
    public @NotNull Type<? extends @NotNull CustomPacketPayload> type() {
        return TYPE;
    }
}
