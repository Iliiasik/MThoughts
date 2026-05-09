package mt.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
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
        boolean hideThemeSwitchButton
) implements CustomPacketPayload {
    public static final ResourceLocation ID_LOC = ResourceLocation.fromNamespaceAndPath("midnightthoughts", "sync_config");
    public static final CustomPacketPayload.Type<SyncConfigPacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);

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
                buf.writeBoolean(p.hideThemeSwitchButton());
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
                    buf.readBoolean()
            )
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}