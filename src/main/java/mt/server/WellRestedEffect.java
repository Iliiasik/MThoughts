package mt.server;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public class WellRestedEffect {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final UUID SPEED_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID STRENGTH_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    private static final UUID HEALTH_UUID = UUID.fromString("e5f6a7b8-c9d0-1234-efab-345678901234");

    private static final String KEY_TICKS = "mt_well_rested_ticks_remaining";
    private static final String KEY_LEVEL = "mt_well_rested_level";
    private static final String KEY_PHASE = "mt_well_rested_phase";
    private static final String KEY_MVP = "mt_well_rested_mvp_flag";

    public static void register() {
    }

    private static boolean dataContains(ServerPlayerEntity player, String key) {
        String prefix = key + "=";
        for (String tag : player.getCommandTags()) {
            if (tag.startsWith(prefix)) return true;
        }
        return false;
    }

    private static int dataGetInt(ServerPlayerEntity player, String key, int def) {
        String prefix = key + "=";
        for (String tag : player.getCommandTags()) {
            if (tag.startsWith(prefix)) {
                try {
                    return Integer.parseInt(tag.substring(prefix.length()));
                } catch (NumberFormatException e) {
                    return def;
                }
            }
        }
        return def;
    }

    private static void dataPutInt(ServerPlayerEntity player, String key, int value) {
        dataRemove(player, key);
        player.getCommandTags().add(key + "=" + value);
    }

    private static void dataRemove(ServerPlayerEntity player, String key) {
        String prefix = key + "=";
        player.getCommandTags().removeIf(tag -> tag.startsWith(prefix));
    }

    private static int clampLevel(int level) {
        if (level < 1) return 1;
        return Math.min(level, 5);
    }

    public static int getTotalDurationTicks(int comfortLevel) {
        return CONFIG.getWellRested().getLevel(clampLevel(comfortLevel)).durationMinutes * 60 * 20;
    }

    public static void applyToPlayer(ServerPlayerEntity player, int comfortLevel) {
        if (comfortLevel <= 0) return;
        int lvlIdx = clampLevel(comfortLevel);
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(lvlIdx);

        removeFromPlayer(player);

        dataPutInt(player, KEY_LEVEL, lvlIdx);
        dataPutInt(player, KEY_TICKS, getTotalDurationTicks(lvlIdx));

        applyHealthBonus(player, lvl.healthBonus);
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.attackSpeedPhase1);

        player.setHealth(player.getMaxHealth());
    }

    public static void removeFromPlayer(ServerPlayerEntity player) {
        dataRemove(player, KEY_LEVEL);
        dataRemove(player, KEY_TICKS);
        dataRemove(player, KEY_PHASE);
        dataRemove(player, KEY_MVP);
        removeAttributes(player);
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
    }

    public static void tick(ServerPlayerEntity player) {
        if (!dataContains(player, KEY_TICKS)) return;

        int ticksRemaining = dataGetInt(player, KEY_TICKS, 0);
        int level = dataGetInt(player, KEY_LEVEL, 1);

        if (ticksRemaining <= 0) {
            removeFromPlayer(player);
            return;
        }

        ticksRemaining--;
        dataPutInt(player, KEY_TICKS, ticksRemaining);

        int totalTicks = getTotalDurationTicksForPlayer(player);
        int phaseTicks = totalTicks / 3;
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(level);

        int currentPhaseIndex = phaseTicks > 0 ? (totalTicks - ticksRemaining) / phaseTicks : 0;
        if (currentPhaseIndex > 2) currentPhaseIndex = 2;

        float speed, strength, attackSpeed, regen;
        switch (currentPhaseIndex) {
            case 0 -> { speed = lvl.speedPhase1; strength = lvl.strengthPhase1; attackSpeed = lvl.attackSpeedPhase1; regen = lvl.regenBonus; }
            case 1 -> { speed = lvl.speedPhase2; strength = lvl.strengthPhase2; attackSpeed = lvl.attackSpeedPhase2; regen = lvl.regenBonus; }
            default -> { speed = lvl.speedPhase3; strength = lvl.strengthPhase3; attackSpeed = lvl.attackSpeedPhase3; regen = lvl.regenBonus; }
        }

        int previousPhase = dataGetInt(player, KEY_PHASE, -1);

        if (previousPhase != currentPhaseIndex) {
            dataPutInt(player, KEY_PHASE, currentPhaseIndex);
            removePhaseAttributes(player);
            applyPhaseAttributes(player, speed, strength, attackSpeed);
        }

        if (regen > 0 && ticksRemaining % 100 == 0) {
            float maxHealth = player.getMaxHealth();
            float current = player.getHealth();
            if (current < maxHealth) {
                player.setHealth(Math.min(current + maxHealth * regen * 0.5f, maxHealth));
            }
        }
    }

    private static void applyHealthBonus(ServerPlayerEntity player, float bonus) {
        var healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null && healthAttr.getModifier(HEALTH_UUID) == null) {
            healthAttr.addPersistentModifier(new EntityAttributeModifier(
                    HEALTH_UUID, "well_rested_health", bonus, EntityAttributeModifier.Operation.ADDITION));
        }
    }

    private static void applyPhaseAttributes(ServerPlayerEntity player, float speed, float strength, float attackSpeed) {
        var speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        var strengthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        var attackSpeedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (speedAttr != null && speedAttr.getModifier(SPEED_UUID) == null)
            speedAttr.addPersistentModifier(new EntityAttributeModifier(SPEED_UUID, "well_rested_speed", speed, EntityAttributeModifier.Operation.MULTIPLY_BASE));
        if (strengthAttr != null && strengthAttr.getModifier(STRENGTH_UUID) == null)
            strengthAttr.addPersistentModifier(new EntityAttributeModifier(STRENGTH_UUID, "well_rested_strength", strength, EntityAttributeModifier.Operation.MULTIPLY_BASE));
        if (attackSpeedAttr != null && attackSpeedAttr.getModifier(ATTACK_SPEED_UUID) == null)
            attackSpeedAttr.addPersistentModifier(new EntityAttributeModifier(ATTACK_SPEED_UUID, "well_rested_attack_speed", attackSpeed, EntityAttributeModifier.Operation.MULTIPLY_BASE));
    }

    private static void removePhaseAttributes(ServerPlayerEntity player) {
        var speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        var strengthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        var attackSpeedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_UUID);
        if (strengthAttr != null) strengthAttr.removeModifier(STRENGTH_UUID);
        if (attackSpeedAttr != null) attackSpeedAttr.removeModifier(ATTACK_SPEED_UUID);
    }

    private static void removeAttributes(ServerPlayerEntity player) {
        removePhaseAttributes(player);
        var healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) healthAttr.removeModifier(HEALTH_UUID);
    }

    public static boolean isMvp(ServerPlayerEntity player) {
        return dataGetInt(player, KEY_MVP, 0) == 1;
    }

    public static void applyMvpToPlayer(ServerPlayerEntity player) {
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(5);
        int durationTicks = CONFIG.getMvp().mvpWellRestedDurationMinutes * 60 * 20;

        removeFromPlayer(player);

        dataPutInt(player, KEY_LEVEL, 5);
        dataPutInt(player, KEY_TICKS, durationTicks);
        dataPutInt(player, KEY_MVP, 1);

        applyHealthBonus(player, lvl.healthBonus);
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.attackSpeedPhase1);

        player.setHealth(player.getMaxHealth());
    }

    public static int getTotalDurationTicksForPlayer(ServerPlayerEntity player) {
        if (isMvp(player)) {
            return CONFIG.getMvp().mvpWellRestedDurationMinutes * 60 * 20;
        }
        return getTotalDurationTicks(getLevel(player));
    }

    public static boolean hasEffect(ServerPlayerEntity player) {
        return dataContains(player, KEY_TICKS) && dataGetInt(player, KEY_TICKS, 0) > 0;
    }

    public static int getTicksRemaining(ServerPlayerEntity player) {
        return dataGetInt(player, KEY_TICKS, 0);
    }

    public static int getLevel(ServerPlayerEntity player) {
        return dataGetInt(player, KEY_LEVEL, 0);
    }

    public static int getCurrentPhase(ServerPlayerEntity player) {
        return dataGetInt(player, KEY_PHASE, 0);
    }
}