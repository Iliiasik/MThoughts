package mt.server;

import mt.MidnightThoughts;
import mt.mixin.HungerManagerAccessor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class WellRestedEffect extends StatusEffect {

    public static RegistryEntry<StatusEffect> WELL_RESTED;

    private static final Identifier HEALTH_MODIFIER_ID = Identifier.of(MidnightThoughts.MOD_ID, "well_rested_health");
    private static final Identifier LUCK_MODIFIER_ID = Identifier.of(MidnightThoughts.MOD_ID, "well_rested_luck");

    public WellRestedEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0xFFD700);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            int level = amplifier + 1;
            float exhaustionReduction = getExhaustionReduction(level);

            HungerManagerAccessor hungerManager = (HungerManagerAccessor) player.getHungerManager();
            float currentExhaustion = hungerManager.getExhaustion();

            if (currentExhaustion > 0) {
                float reducedExhaustion = Math.max(0, currentExhaustion - exhaustionReduction);
                hungerManager.setExhaustion(reducedExhaustion);
            }
            return true;
        }
        return false;
    }

    private float getExhaustionReduction(int level) {
        return switch (level) {
            case 1 -> 0.05f;
            case 2 -> 0.1f;
            case 3 -> 0.15f;
            case 4 -> 0.2f;
            default -> 0.25f;
        };
    }

    public static int getDurationForLevel(int level) {
        return switch (level) {
            case 1 -> 3 * 60 * 20;
            case 2 -> 5 * 60 * 20;
            case 3 -> 7 * 60 * 20;
            case 4 -> 10 * 60 * 20;
            default -> 15 * 60 * 20;
        };
    }

    public static void register() {
        WellRestedEffect effect = new WellRestedEffect();

        effect.addAttributeModifier(
                EntityAttributes.MAX_HEALTH,
                HEALTH_MODIFIER_ID,
                2.0,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        effect.addAttributeModifier(
                EntityAttributes.LUCK,
                LUCK_MODIFIER_ID,
                0.25,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        WELL_RESTED = Registry.registerReference(
                Registries.STATUS_EFFECT,
                Identifier.of(MidnightThoughts.MOD_ID, "well_rested"),
                effect
        );
    }

    public static void applyToPlayer(ServerPlayerEntity player, int comfortLevel) {
        if (comfortLevel <= 0) {
            return;
        }

        int duration = getDurationForLevel(comfortLevel);
        int amplifier = comfortLevel - 1;

        StatusEffectInstance instance = new StatusEffectInstance(
                WELL_RESTED,
                duration,
                amplifier,
                true,
                false,
                true
        );

        player.addStatusEffect(instance);

        if (comfortLevel >= 3) {
            player.setHealth(player.getHealth() + 4.0f);
        } else {
            player.setHealth(player.getHealth() + 2.0f);
        }
    }
}