package mt.server;

import mt.client.config.MidnightThoughtsConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WellRestedEffect extends MobEffect {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    public WellRestedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
        MidnightThoughtsConfig.WellRestedLevel level1 = CONFIG.getWellRested().getLevel(1);
        addAttributeModifier(
                Attributes.MAX_HEALTH,
                "00d1c7eb-9161-4f36-bee2-06e39a2d491b",
                level1.healthBonus,
                AttributeModifier.Operation.ADDITION
        );
        addAttributeModifier(
                Attributes.LUCK,
                "5c7e1afb-61f1-4a83-b667-cd92fda75571",
                level1.luckBonus,
                AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(net.minecraft.world.entity.LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayer player) {
            int level = amplifier + 1;
            float exhaustionReduction = getExhaustionReduction(level);
            if (player.getFoodData().getExhaustionLevel() > 0) {
                float currentExhaustion = player.getFoodData().getExhaustionLevel();
                float reducedExhaustion = Math.max(0, currentExhaustion - exhaustionReduction);
                player.getFoodData().setExhaustion(reducedExhaustion);
            }
        }
    }

    private float getExhaustionReduction(int level) {
        return CONFIG.getWellRested().getLevel(level).exhaustionReduction;
    }

    public static int getDurationForLevel(int level) {
        int durationMinutes = CONFIG.getWellRested().getLevel(level).durationMinutes;
        return durationMinutes * 60 * 20;
    }

    public static void applyToPlayer(ServerPlayer player, int comfortLevel, MobEffect effect) {
        if (comfortLevel <= 0) {
            return;
        }
        int duration = getDurationForLevel(comfortLevel);
        int amplifier = comfortLevel - 1;
        MobEffectInstance instance = new MobEffectInstance(
                effect,
                duration,
                amplifier,
                true,
                false,
                true
        );
        player.addEffect(instance);
        float instantHeal = CONFIG.getWellRested().getLevel(comfortLevel).instantHeal;
        player.setHealth(player.getHealth() + instantHeal);
    }
}