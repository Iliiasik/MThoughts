package mt.network.packet;

import mt.server.AchievementDefinition;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record SyncAchievementsPacket(List<AchievementDefinition> achievements) implements CustomPacketPayload {
    public static final Identifier ID_LOC = Identifier.fromNamespaceAndPath("midnightthoughts", "sync_achievements");
    public static final CustomPacketPayload.Type<@NotNull SyncAchievementsPacket> TYPE = new CustomPacketPayload.Type<>(ID_LOC);
    public static final StreamCodec<FriendlyByteBuf, SyncAchievementsPacket> CODEC = StreamCodec.of(
            (buf, packet) -> {
                buf.writeVarInt(packet.achievements().size());
                for (AchievementDefinition def : packet.achievements()) {
                    buf.writeUtf(def.id);
                    buf.writeUtf(def.name);
                    buf.writeUtf(def.tooltip != null ? def.tooltip : "");
                }
            },
            buf -> {
                int size = buf.readVarInt();
                List<AchievementDefinition> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    AchievementDefinition def = new AchievementDefinition();
                    def.id = buf.readUtf();
                    def.name = buf.readUtf();
                    String tooltip = buf.readUtf();
                    def.tooltip = tooltip.isEmpty() ? null : tooltip;
                    list.add(def);
                }
                return new SyncAchievementsPacket(list);
            }
    );

    @Override
    public @NotNull Type<@NotNull ? extends CustomPacketPayload> type() {
        return TYPE;
    }
}