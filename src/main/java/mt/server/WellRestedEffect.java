package mt.server;

import mt.client.config.MidnightThoughtsConfig;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.Identifier;

public class WellRestedEffect {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final Identifier SPEED_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_speed");
    private static final Identifier STRENGTH_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_strength");
    private static final Identifier ATTACK_SPEED_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_attack_speed");
    private static final Identifier HASTE_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_haste");
    private static final Identifier HEALTH_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_health");

    private static int clampLevel(int level) {
        if (level < 1) return 1;
        if (level > 5) return 5;
        return level;
    }

    public static int getTotalDurationTicks(int comfortLevel) {
        return CONFIG.getWellRested().getLevel(clampLevel(comfortLevel)).durationMinutes * 60 * 20;
    }

    public static void applyToPlayer(ServerPlayer player, int comfortLevel) {
        if (comfortLevel <= 0) return;
        int lvlIdx = clampLevel(comfortLevel);
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(lvlIdx);

        removeFromPlayer(player);

        player.getPersistentData().putInt("mt_well_rested_level", lvlIdx);
        player.getPersistentData().putInt("mt_well_rested_ticks_remaining", getTotalDurationTicks(lvlIdx));

        applyHealthBonus(player, lvl.healthBonus);
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.hastePhase1, lvl.attackSpeedPhase1);

        player.setHealth(player.getMaxHealth());
    }

    public static void removeFromPlayer(ServerPlayer player) {
        player.getPersistentData().remove("mt_well_rested_level");
        player.getPersistentData().remove("mt_well_rested_ticks_remaining");
        player.getPersistentData().remove("mt_well_rested_phase");
        player.getPersistentData().remove("mt_well_rested_mvp_flag");
        removeAttributes(player);
    }

    public static void tick(ServerPlayer player) {
        if (!player.getPersistentData().contains("mt_well_rested_ticks_remaining")) return;

        int ticksRemaining = player.getPersistentData().getIntOr("mt_well_rested_ticks_remaining", 0);
        int level = player.getPersistentData().getIntOr("mt_well_rested_level", 1);

        if (ticksRemaining <= 0) {
            removeFromPlayer(player);
            syncAttributes(player);
            return;
        }

        ticksRemaining--;
        player.getPersistentData().putInt("mt_well_rested_ticks_remaining", ticksRemaining);

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

        int previousPhase = player.getPersistentData().getIntOr("mt_well_rested_phase", -1);

        if (previousPhase != currentPhaseIndex) {
            player.getPersistentData().putInt("mt_well_rested_phase", currentPhaseIndex);
            applyPhaseAttributes(player, speed, strength, haste, attackSpeed);
            syncAttributes(player);
        }

        if (regen > 0 && ticksRemaining % 100 == 0) {
            float maxHealth = player.getMaxHealth();
            float current = player.getHealth();
            if (current < maxHealth) {
                player.setHealth(Math.min(current + maxHealth * regen * 0.5f, maxHealth));
            }
        }
    }

    private static void syncAttributes(ServerPlayer player) {
        var dirty = player.getAttributes().getAttributesToSync();
        if (!dirty.isEmpty()) {
            player.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), dirty));
            dirty.clear();
        }
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
    }

    private static void applyHealthBonus(ServerPlayer player, float bonus) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.addOrReplacePermanentModifier(new AttributeModifier(HEALTH_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void applyPhaseAttributes(ServerPlayer player, float speed, float strength, float haste, float attackSpeed) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var strengthAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var hasteAttr = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        var attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null) speedAttr.addOrReplacePermanentModifier(new AttributeModifier(SPEED_ID, speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (strengthAttr != null) strengthAttr.addOrReplacePermanentModifier(new AttributeModifier(STRENGTH_ID, strength, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (hasteAttr != null) hasteAttr.addOrReplacePermanentModifier(new AttributeModifier(HASTE_ID, haste, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (attackSpeedAttr != null) attackSpeedAttr.addOrReplacePermanentModifier(new AttributeModifier(ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static void removeAttributes(ServerPlayer player) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var strengthAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var hasteAttr = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        var attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_ID);
        if (strengthAttr != null) strengthAttr.removeModifier(STRENGTH_ID);
        if (hasteAttr != null) hasteAttr.removeModifier(HASTE_ID);
        if (attackSpeedAttr != null) attackSpeedAttr.removeModifier(ATTACK_SPEED_ID);
        if (healthAttr != null) healthAttr.removeModifier(HEALTH_ID);
    }

    public static boolean isMvp(ServerPlayer player) {
        return player.getPersistentData().getIntOr("mt_well_rested_mvp_flag", 0) == 1;
    }

    public static void applyMvpToPlayer(ServerPlayer player) {
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(5);
        int durationTicks = CONFIG.getMvp().mvpWellRestedDurationMinutes * 60 * 20;

        removeFromPlayer(player);

        player.getPersistentData().putInt("mt_well_rested_level", 5);
        player.getPersistentData().putInt("mt_well_rested_ticks_remaining", durationTicks);
        player.getPersistentData().putInt("mt_well_rested_mvp_flag", 1);

        applyHealthBonus(player, lvl.healthBonus);
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.hastePhase1, lvl.attackSpeedPhase1);

        player.setHealth(player.getMaxHealth());
    }

    public static int getTotalDurationTicksForPlayer(ServerPlayer player) {
        if (isMvp(player)) {
            return CONFIG.getMvp().mvpWellRestedDurationMinutes * 60 * 20;
        }
        return getTotalDurationTicks(getLevel(player));
    }

    public static boolean hasEffect(ServerPlayer player) {
        return player.getPersistentData().contains("mt_well_rested_ticks_remaining")
                && player.getPersistentData().getIntOr("mt_well_rested_ticks_remaining", 0) > 0;
    }

    public static int getTicksRemaining(ServerPlayer player) {
        return player.getPersistentData().getIntOr("mt_well_rested_ticks_remaining", 0);
    }

    public static int getLevel(ServerPlayer player) {
        return player.getPersistentData().getIntOr("mt_well_rested_level", 0);
    }

    public static int getCurrentPhase(ServerPlayer player) {
        return player.getPersistentData().getIntOr("mt_well_rested_phase", 0);
    }
}