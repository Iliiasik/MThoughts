package mt.server;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WellRestedData extends SavedData {

    public record Entry(int level, int ticksRemaining, int phase, boolean mvp) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("level").forGetter(Entry::level),
                Codec.INT.fieldOf("ticks_remaining").forGetter(Entry::ticksRemaining),
                Codec.INT.fieldOf("phase").forGetter(Entry::phase),
                Codec.BOOL.fieldOf("mvp").forGetter(Entry::mvp)
        ).apply(i, Entry::new));
    }

    public static final Codec<WellRestedData> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Entry.CODEC)
            .xmap(WellRestedData::new, data -> data.entries);

    public static final SavedDataType<WellRestedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested"),
            WellRestedData::new,
            CODEC,
            DataFixTypes.LEVEL
    );

    private final Map<UUID, Entry> entries;

    public WellRestedData() {
        this.entries = new HashMap<>();
    }

    private WellRestedData(Map<UUID, Entry> loaded) {
        this.entries = new HashMap<>(loaded);
    }

    public static WellRestedData get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public Entry get(UUID uuid) {
        return entries.get(uuid);
    }

    public void put(UUID uuid, Entry entry) {
        entries.put(uuid, entry);
        setDirty();
    }

    public void remove(UUID uuid) {
        if (entries.remove(uuid) != null) {
            setDirty();
        }
    }
}
