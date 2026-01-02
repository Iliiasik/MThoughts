package mt.server;

import mt.MidnightThoughts;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WellRestedEffect extends StatusEffect {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

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
    public boolean applyUpdateEffect(net.minecraft.entity.LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity player) {
            int level = amplifier + 1;
            float exhaustionReduction = getExhaustionReduction(level);

            if (player.getHungerManager().getExhaustion() > 0) {
                float currentExhaustion = player.getHungerManager().getExhaustion();
                float reducedExhaustion = Math.max(0, currentExhaustion - exhaustionReduction);
                player.getHungerManager().setExhaustion(reducedExhaustion);
            }
        }
        return true;
    }

    private float getExhaustionReduction(int level) {
        return switch (level) {
            case 1 -> 0.005f;
            case 2 -> 0.01f;
            case 3 -> 0.015f;
            case 4 -> 0.02f;
            default -> 0.025f;
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
            EntityAttributes.GENERIC_MAX_HEALTH,
            HEALTH_MODIFIER_ID,
            2.0,
            EntityAttributeModifier.Operation.ADD_VALUE
        );

        effect.addAttributeModifier(
            EntityAttributes.GENERIC_LUCK,
            LUCK_MODIFIER_ID,
            0.25,
            EntityAttributeModifier.Operation.ADD_VALUE
        );

        WELL_RESTED = Registry.registerReference(
            Registries.STATUS_EFFECT,
            Identifier.of(MidnightThoughts.MOD_ID, "well_rested"),
            effect
        );
        LOGGER.info("[WellRestedEffect] Registered well_rested effect");
    }

    public static void applyToPlayer(ServerPlayerEntity player, int comfortLevel) {
        if (comfortLevel <= 0) {
            LOGGER.info("[WellRestedEffect] Comfort level 0, no effect applied to {}", player.getGameProfile().getName());
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

        LOGGER.info("[WellRestedEffect] Applied Well Rested level {} ({} seconds) to {}",
            comfortLevel, duration / 20, player.getGameProfile().getName());
    }
}

