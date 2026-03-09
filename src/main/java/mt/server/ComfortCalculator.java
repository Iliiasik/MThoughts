package mt.server;

import mt.client.config.MidnightThoughtsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ComfortCalculator {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final TagKey<Block> LIGHTING_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_lighting"));
    private static final TagKey<Block> CARPET_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_carpet"));
    private static final TagKey<Block> FURNITURE_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_furniture"));
    private static final TagKey<Block> DECOR_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_decoration"));
    private static final TagKey<Block> STRUCTURE_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_structure"));
    private static final TagKey<Block> NEGATIVE_MACABRE_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_negative_macabre"));
    private static final TagKey<Block> NEGATIVE_HOSTILE_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_negative_hostile"));
    private static final TagKey<Block> NEGATIVE_DARK_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("midnightthoughts", "comfort_negative_dark"));

    public static int calculateComfortLevel(ServerPlayer player) {
        if (!CONFIG.getComfort().enabled) {
            return 0;
        }

        int scanRadius = CONFIG.getComfort().scanRadius;
        BlockPos bedPos = player.getSleepingPos().orElse(player.blockPosition());
        Level world = player.level();

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
                    BlockPos checkPos = bedPos.offset(x, y, z);
                    BlockState state = world.getBlockState(checkPos);

                    if (!hasLighting && state.is(LIGHTING_TAG)) hasLighting = true;
                    if (!hasCarpet && state.is(CARPET_TAG)) hasCarpet = true;
                    if (!hasFurniture && state.is(FURNITURE_TAG)) hasFurniture = true;
                    if (!hasDecor && state.is(DECOR_TAG)) hasDecor = true;
                    if (!hasStructure && state.is(STRUCTURE_TAG)) hasStructure = true;
                    if (!hasMacabre && state.is(NEGATIVE_MACABRE_TAG)) hasMacabre = true;
                    if (!hasHostile && state.is(NEGATIVE_HOSTILE_TAG)) hasHostile = true;
                    if (!hasDark && state.is(NEGATIVE_DARK_TAG)) hasDark = true;

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

    public static boolean isNightmareMode(ServerPlayer player) {
        return calculateComfortLevel(player) < 0;
    }

    public static boolean isSleepBlocked(ServerPlayer player) {
        return calculateComfortLevel(player) <= -2;
    }
}