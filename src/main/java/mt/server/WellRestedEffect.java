package mt.server;

import mt.MidnightThoughts;
import mt.client.config.MidnightThoughtsConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WellRestedEffect extends MobEffect {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    public WellRestedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
        MidnightThoughtsConfig.WellRestedLevel level1 = CONFIG.getWellRested().getLevel(1);
        addAttributeModifier(
                Attributes.MAX_HEALTH,
                Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_health"),
                level1.healthBonus,
                AttributeModifier.Operation.ADD_VALUE
        );
        addAttributeModifier(
                Attributes.LUCK,
                Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_luck"),
                level1.luckBonus,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            int level = amplifier + 1;
            float exhaustionReduction = getExhaustionReduction(level);
            player.causeFoodExhaustion(-exhaustionReduction);
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

    public static void applyToPlayer(ServerPlayer player, int comfortLevel) {
        if (comfortLevel <= 0) return;
        int duration = getDurationForLevel(comfortLevel);
        int amplifier = comfortLevel - 1;
        MobEffectInstance instance = new MobEffectInstance(MidnightThoughts.WELL_RESTED, duration, amplifier, true, false, true);
        player.addEffect(instance);
        float instantHeal = CONFIG.getWellRested().getLevel(comfortLevel).instantHeal;
        player.setHealth(player.getHealth() + instantHeal);
    }
}