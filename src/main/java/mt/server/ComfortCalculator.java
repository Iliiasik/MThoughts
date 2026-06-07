package mt.server;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComfortCalculator {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();
    private static final int RECALCULATE_INTERVAL = 100;

    private static final TagKey<Block> LIGHTING_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_lighting"));
    private static final TagKey<Block> CARPET_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_carpet"));
    private static final TagKey<Block> FURNITURE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_furniture"));
    private static final TagKey<Block> DECOR_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_decoration"));
    private static final TagKey<Block> STRUCTURE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_structure"));
    private static final TagKey<Block> NEGATIVE_MACABRE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_negative_macabre"));
    private static final TagKey<Block> NEGATIVE_HOSTILE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_negative_hostile"));
    private static final TagKey<Block> NEGATIVE_DARK_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_negative_dark"));

    @SuppressWarnings("unchecked")
    private static final TagKey<Block>[] ALL_TAGS = (TagKey<Block>[]) new TagKey[]{
            LIGHTING_TAG, CARPET_TAG, FURNITURE_TAG, DECOR_TAG, STRUCTURE_TAG,
            NEGATIVE_MACABRE_TAG, NEGATIVE_HOSTILE_TAG, NEGATIVE_DARK_TAG
    };

    private record CachedComfort(int comfortLevel, long calculatedAtTick, BlockPos pos) {}
    private static final Map<UUID, CachedComfort> cache = new HashMap<>();

    public static int calculateComfortLevel(ServerPlayerEntity player) {
        if (!CONFIG.getComfort().enabled) {
            return 0;
        }

        long currentTick = player.getEntityWorld().getTime();
        BlockPos currentPos = player.getBlockPos();
        UUID uuid = player.getUuid();

        CachedComfort cached = cache.get(uuid);
        if (cached != null
                && (currentTick - cached.calculatedAtTick()) < RECALCULATE_INTERVAL
                && cached.pos().equals(currentPos)) {
            return cached.comfortLevel();
        }

        int scanRadius = CONFIG.getComfort().scanRadius;
        BlockPos bedPos = player.getSleepingPosition().orElse(currentPos);
        World world = player.getEntityWorld();

        boolean[] found = new boolean[ALL_TAGS.length];
        outer:
        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    BlockState state = world.getBlockState(bedPos.add(x, y, z));
                    boolean allFound = true;
                    for (int i = 0; i < ALL_TAGS.length; i++) {
                        if (!found[i] && state.isIn(ALL_TAGS[i])) found[i] = true;
                        if (!found[i]) allFound = false;
                    }
                    if (allFound) break outer;
                }
            }
        }

        int comfortLevel = 0;
        if (found[0]) comfortLevel++;
        if (found[1]) comfortLevel++;
        if (found[2]) comfortLevel++;
        if (found[3]) comfortLevel++;
        if (found[4]) comfortLevel++;
        if (found[5]) comfortLevel--;
        if (found[6]) comfortLevel--;
        if (found[7]) comfortLevel--;

        cache.put(uuid, new CachedComfort(comfortLevel, currentTick, currentPos));
        return comfortLevel;
    }

    public static boolean isNightmareMode(ServerPlayerEntity player) {
        return calculateComfortLevel(player) < 0;
    }

    public static boolean isSleepBlocked(ServerPlayerEntity player) {
        return calculateComfortLevel(player) <= -2;
    }

    public static void invalidateCache(ServerPlayerEntity player) {
        cache.remove(player.getUuid());
    }

}