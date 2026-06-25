package mt.server;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class WellRestedEffect {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final Identifier SPEED_ID = Identifier.of("midnightthoughts", "well_rested_speed");
    private static final Identifier STRENGTH_ID = Identifier.of("midnightthoughts", "well_rested_strength");
    private static final Identifier ATTACK_SPEED_ID = Identifier.of("midnightthoughts", "well_rested_attack_speed");
    private static final Identifier HASTE_ID = Identifier.of("midnightthoughts", "well_rested_haste");
    private static final Identifier HEALTH_ID = Identifier.of("midnightthoughts", "well_rested_health");

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
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.hastePhase1, lvl.attackSpeedPhase1);

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

        float speed, strength, haste, attackSpeed, regen;
        switch (currentPhaseIndex) {
            case 0 -> { speed = lvl.speedPhase1; strength = lvl.strengthPhase1; haste = lvl.hastePhase1; attackSpeed = lvl.attackSpeedPhase1; regen = lvl.regenBonus; }
            case 1 -> { speed = lvl.speedPhase2; strength = lvl.strengthPhase2; haste = lvl.hastePhase2; attackSpeed = lvl.attackSpeedPhase2; regen = lvl.regenBonus; }
            default -> { speed = lvl.speedPhase3; strength = lvl.strengthPhase3; haste = lvl.hastePhase3; attackSpeed = lvl.attackSpeedPhase3; regen = lvl.regenBonus; }
        }

        int previousPhase = dataGetInt(player, KEY_PHASE, -1);

        if (previousPhase != currentPhaseIndex) {
            dataPutInt(player, KEY_PHASE, currentPhaseIndex);
            removePhaseAttributes(player);
            applyPhaseAttributes(player, speed, strength, haste, attackSpeed);
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
        if (healthAttr != null && healthAttr.getModifier(HEALTH_ID) == null) {
            healthAttr.addPersistentModifier(new EntityAttributeModifier(
                    HEALTH_ID, bonus, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void applyPhaseAttributes(ServerPlayerEntity player, float speed, float strength, float haste, float attackSpeed) {
        var speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        var strengthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        var hasteAttr = player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
        var attackSpeedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (speedAttr != null && speedAttr.getModifier(SPEED_ID) == null)
            speedAttr.addPersistentModifier(new EntityAttributeModifier(SPEED_ID, speed, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (strengthAttr != null && strengthAttr.getModifier(STRENGTH_ID) == null)
            strengthAttr.addPersistentModifier(new EntityAttributeModifier(STRENGTH_ID, strength, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (hasteAttr != null && hasteAttr.getModifier(HASTE_ID) == null)
            hasteAttr.addPersistentModifier(new EntityAttributeModifier(HASTE_ID, haste, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (attackSpeedAttr != null && attackSpeedAttr.getModifier(ATTACK_SPEED_ID) == null)
            attackSpeedAttr.addPersistentModifier(new EntityAttributeModifier(ATTACK_SPEED_ID, attackSpeed, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static void removePhaseAttributes(ServerPlayerEntity player) {
        var speedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        var strengthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        var hasteAttr = player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_BREAK_SPEED);
        var attackSpeedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_ID);
        if (strengthAttr != null) strengthAttr.removeModifier(STRENGTH_ID);
        if (hasteAttr != null) hasteAttr.removeModifier(HASTE_ID);
        if (attackSpeedAttr != null) attackSpeedAttr.removeModifier(ATTACK_SPEED_ID);
    }

    private static void removeAttributes(ServerPlayerEntity player) {
        removePhaseAttributes(player);
        var healthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (healthAttr != null) healthAttr.removeModifier(HEALTH_ID);
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
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.hastePhase1, lvl.attackSpeedPhase1);

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