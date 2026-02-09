package mt.server;

import mt.MidnightThoughts;
import mt.config.MidnightThoughtsConfig;
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

public class WellRestedEffect extends StatusEffect {

    public static RegistryEntry<StatusEffect> WELL_RESTED;
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

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
        return CONFIG.getWellRested().getLevel(level).exhaustionReduction;
    }

    public static int getDurationForLevel(int level) {
        int durationMinutes = CONFIG.getWellRested().getLevel(level).durationMinutes;
        return durationMinutes * 60 * 20;
    }

    public static void register() {
        WellRestedEffect effect = new WellRestedEffect();

        MidnightThoughtsConfig.WellRestedLevel level1 = CONFIG.getWellRested().getLevel(1);
        effect.addAttributeModifier(
            EntityAttributes.GENERIC_MAX_HEALTH,
            HEALTH_MODIFIER_ID,
            level1.healthBonus,
            EntityAttributeModifier.Operation.ADD_VALUE
        );

        effect.addAttributeModifier(
            EntityAttributes.GENERIC_LUCK,
            LUCK_MODIFIER_ID,
            level1.luckBonus,
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

        float instantHeal = CONFIG.getWellRested().getLevel(comfortLevel).instantHeal;
        player.setHealth(player.getHealth() + instantHeal);
    }
}