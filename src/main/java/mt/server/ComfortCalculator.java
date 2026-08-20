package mt.server;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ComfortCalculator {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();
    private static final int RECALCULATE_INTERVAL = 100;
    private static final int MAX_SCAN_RADIUS = 8;

    private static final TagKey<Block> LIGHTING_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_lighting"));
    private static final TagKey<Block> CARPET_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_carpet"));
    private static final TagKey<Block> FURNITURE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_furniture"));
    private static final TagKey<Block> DECOR_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_decoration"));
    private static final TagKey<Block> STRUCTURE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_structure"));
    private static final TagKey<Block> NEGATIVE_MACABRE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_negative_macabre"));
    private static final TagKey<Block> NEGATIVE_HOSTILE_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_negative_hostile"));
    private static final TagKey<Block> NEGATIVE_DARK_TAG = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_negative_dark"));

    @SuppressWarnings("unchecked")
    private static final TagKey<Block>[] ALL_TAGS = (TagKey<Block>[]) new TagKey[]{
            LIGHTING_TAG, CARPET_TAG, FURNITURE_TAG, DECOR_TAG, STRUCTURE_TAG,
            NEGATIVE_MACABRE_TAG, NEGATIVE_HOSTILE_TAG, NEGATIVE_DARK_TAG
    };

    public static final String[] CATEGORY_NAMES = {
            "lighting", "carpet", "furniture", "decoration", "structure", "macabre", "hostile", "dark"
    };

    private record CachedComfort(int comfortLevel, long calculatedAtTick, BlockPos pos) {}
    private static final Map<UUID, CachedComfort> cache = new HashMap<>();

    public record ComfortDebug(boolean enabled, int scanRadius, boolean[] found, int[] weights, int total,
                               int nightmareThreshold, int sleepBlockThreshold,
                               boolean nightmareEnabled, boolean sleepBlockEnabled,
                               boolean nightmare, boolean sleepBlocked) {}

    private static int[] currentWeights() {
        MidnightThoughtsConfig.ComfortWeights w = CONFIG.getComfort().weights;
        return new int[]{ w.lighting, w.carpet, w.furniture, w.decoration, w.structure, w.macabre, w.hostile, w.dark };
    }

    @SuppressWarnings("resource")
    private static boolean[] scanFound(ServerPlayer player) {
        int scanRadius = Math.clamp(CONFIG.getComfort().scanRadius, 0, MAX_SCAN_RADIUS);
        BlockPos bedPos = player.getSleepingPos().orElse(player.blockPosition());
        Level world = player.level();

        boolean[] found = new boolean[ALL_TAGS.length];
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        outer:
        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    cursor.setWithOffset(bedPos, x, y, z);
                    BlockState state = world.getBlockState(cursor);
                    boolean allFound = true;
                    for (int i = 0; i < ALL_TAGS.length; i++) {
                        if (!found[i] && state.is(ALL_TAGS[i])) found[i] = true;
                        if (!found[i]) allFound = false;
                    }
                    if (allFound) break outer;
                }
            }
        }
        return found;
    }

    @SuppressWarnings("resource")
    public static int calculateComfortLevel(ServerPlayer player) {
        if (!CONFIG.getComfort().enabled) {
            return 0;
        }

        long currentTick = player.level().getGameTime();
        BlockPos currentPos = player.blockPosition();
        UUID uuid = player.getUUID();

        CachedComfort cached = cache.get(uuid);
        if (cached != null
                && (currentTick - cached.calculatedAtTick()) < RECALCULATE_INTERVAL
                && cached.pos().equals(currentPos)) {
            return cached.comfortLevel();
        }

        boolean[] found = scanFound(player);
        int[] weights = currentWeights();
        int comfortLevel = 0;
        for (int i = 0; i < ALL_TAGS.length; i++) {
            if (found[i]) comfortLevel += weights[i];
        }

        cache.put(uuid, new CachedComfort(comfortLevel, currentTick, currentPos));

        comfortLevel = mt.api.event.ComfortCalculatedCallback.EVENT.invoker()
                .onComfortCalculated(player, comfortLevel);

        cache.put(uuid, new CachedComfort(comfortLevel, currentTick, currentPos));
        return comfortLevel;
    }

    public static ComfortDebug debug(ServerPlayer player) {
        MidnightThoughtsConfig.ComfortSettings c = CONFIG.getComfort();
        int scanRadius = Math.clamp(c.scanRadius, 0, MAX_SCAN_RADIUS);
        int[] weights = currentWeights();
        boolean[] found = c.enabled ? scanFound(player) : new boolean[ALL_TAGS.length];
        int total = 0;
        if (c.enabled) {
            for (int i = 0; i < found.length; i++) {
                if (found[i]) total += weights[i];
            }
        }
        boolean nightmare = c.enabled && c.nightmareEnabled && total <= c.nightmareThreshold;
        boolean sleepBlocked = c.enabled && c.sleepBlockEnabled && total <= c.sleepBlockThreshold;
        return new ComfortDebug(c.enabled, scanRadius, found, weights, total,
                c.nightmareThreshold, c.sleepBlockThreshold, c.nightmareEnabled, c.sleepBlockEnabled,
                nightmare, sleepBlocked);
    }

    public static boolean isNightmareMode(ServerPlayer player) {
        if (!CONFIG.getComfort().nightmareEnabled) return false;
        return calculateComfortLevel(player) <= CONFIG.getComfort().nightmareThreshold;
    }

    public static boolean isSleepBlocked(ServerPlayer player) {
        if (!CONFIG.getComfort().sleepBlockEnabled) return false;
        return calculateComfortLevel(player) <= CONFIG.getComfort().sleepBlockThreshold;
    }

    public static void invalidateCache(ServerPlayer player) {
        cache.remove(player.getUUID());
    }

    public static void clearCache() {
        cache.clear();
    }
}
