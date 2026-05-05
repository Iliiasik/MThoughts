package mt.network.packet;

import mt.server.AchievementDefinition;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record SyncAchievementsPacket(List<AchievementDefinition> achievements) implements CustomPayload {

    public static final Identifier ID_LOC = Identifier.of("midnightthoughts", "sync_achievements");
    public static final Id<@NotNull SyncAchievementsPacket> ID = new Id<>(ID_LOC);

    public static final PacketCodec<PacketByteBuf, SyncAchievementsPacket> CODEC = PacketCodec.of(
            (packet, buf) -> {
                buf.writeInt(packet.achievements().size());
                for (AchievementDefinition def : packet.achievements()) {
                    buf.writeString(def.id);
                    buf.writeString(def.name);
                    buf.writeString(def.tooltip != null ? def.tooltip : "");
                }
            },
            buf -> {
                int size = buf.readInt();
                List<AchievementDefinition> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    AchievementDefinition def = new AchievementDefinition();
                    def.id = buf.readString();
                    def.name = buf.readString();
                    String tooltip = buf.readString();
                    def.tooltip = tooltip.isEmpty() ? null : tooltip;
                    list.add(def);
                }
                return new SyncAchievementsPacket(list);
            }
    );

    @Override
    public @NotNull Id<? extends CustomPayload> getId() {
        return ID;
    }
}