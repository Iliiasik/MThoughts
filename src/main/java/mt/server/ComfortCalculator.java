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

public class ComfortCalculator {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final TagKey<Block> LIGHTING_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_lighting"));
    private static final TagKey<Block> CARPET_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_carpet"));
    private static final TagKey<Block> FURNITURE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_furniture"));
    private static final TagKey<Block> DECOR_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_decoration"));
    private static final TagKey<Block> STRUCTURE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_structure"));
    private static final TagKey<Block> NEGATIVE_MACABRE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_negative_macabre"));
    private static final TagKey<Block> NEGATIVE_HOSTILE_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_negative_hostile"));
    private static final TagKey<Block> NEGATIVE_DARK_TAG = TagKey.of(RegistryKeys.BLOCK, Identifier.of("midnightthoughts", "comfort_negative_dark"));

    public static int calculateComfortLevel(ServerPlayerEntity player) {
        if (!CONFIG.getComfort().enabled) {
            return 0;
        }

        int scanRadius = CONFIG.getComfort().scanRadius;
        BlockPos bedPos = player.getSleepingPosition().orElse(player.getBlockPos());
        World world = player.getWorld();

        boolean hasLighting = false;
        boolean hasCarpet = false;
        boolean hasFurniture = false;
        boolean hasDecor = false;
        boolean hasStructure = false;
        boolean hasMacabre = false;
        boolean hasHostile = false;
        boolean hasDark = false;

        for (int x = -scanRadius; x <= scanRadius; x++) {
            for (int y = -scanRadius; y <= scanRadius; y++) {
                for (int z = -scanRadius; z <= scanRadius; z++) {
                    BlockPos checkPos = bedPos.add(x, y, z);
                    BlockState state = world.getBlockState(checkPos);

                    if (!hasLighting && state.isIn(LIGHTING_TAG)) hasLighting = true;
                    if (!hasCarpet && state.isIn(CARPET_TAG)) hasCarpet = true;
                    if (!hasFurniture && state.isIn(FURNITURE_TAG)) hasFurniture = true;
                    if (!hasDecor && state.isIn(DECOR_TAG)) hasDecor = true;
                    if (!hasStructure && state.isIn(STRUCTURE_TAG)) hasStructure = true;
                    if (!hasMacabre && state.isIn(NEGATIVE_MACABRE_TAG)) hasMacabre = true;
                    if (!hasHostile && state.isIn(NEGATIVE_HOSTILE_TAG)) hasHostile = true;
                    if (!hasDark && state.isIn(NEGATIVE_DARK_TAG)) hasDark = true;

                    if (hasLighting && hasCarpet && hasFurniture && hasDecor && hasStructure
                            && hasMacabre && hasHostile && hasDark) break;
                }
            }
        }

        int comfortLevel = 0;
        if (hasLighting) comfortLevel++;
        if (hasCarpet) comfortLevel++;
        if (hasFurniture) comfortLevel++;
        if (hasDecor) comfortLevel++;
        if (hasStructure) comfortLevel++;

        if (hasMacabre) comfortLevel--;
        if (hasHostile) comfortLevel--;
        if (hasDark) comfortLevel--;

        return comfortLevel;
    }

    public static boolean isNightmareMode(ServerPlayerEntity player) {
        return calculateComfortLevel(player) < 0;
    }

    public static boolean isSleepBlocked(ServerPlayerEntity player) {
        return calculateComfortLevel(player) <= -2;
    }
}