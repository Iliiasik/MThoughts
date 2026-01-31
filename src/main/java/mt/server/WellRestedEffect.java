package mt.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WellRestedEffect extends MobEffect {
    public WellRestedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFD700);
        addAttributeModifier(
                Attributes.MAX_HEALTH,
                "00d1c7eb-9161-4f36-bee2-06e39a2d491b",
                2.0,
                AttributeModifier.Operation.ADDITION
        );
        addAttributeModifier(
                Attributes.LUCK,
                "5c7e1afb-61f1-4a83-b667-cd92fda75571",
                0.25,
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
        if (comfortLevel >= 3) {
            player.setHealth(player.getHealth() + 4.0f);
        } else {
            player.setHealth(player.getHealth() + 2.0f);
        }
    }
}