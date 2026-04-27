package mt.server;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WellRestedEffect {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final Identifier SPEED_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_speed");
    private static final Identifier STRENGTH_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_strength");
    private static final Identifier ATTACK_SPEED_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_attack_speed");
    private static final Identifier HASTE_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_haste");
    private static final Identifier HEALTH_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_health");

    private static final Map<UUID, Integer> ticksRemainingMap = new HashMap<>();
    private static final Map<UUID, Integer> levelMap = new HashMap<>();
    private static final Map<UUID, Integer> phaseMap = new HashMap<>();
    private static final Map<UUID, Boolean> mvpMap = new HashMap<>();

    public static void register() {
    }

    private static int clampLevel(int level) {
        if (level < 1) return 1;
        return Math.min(level, 5);
    }

    public static int getTotalDurationTicks(int comfortLevel) {
        return CONFIG.getWellRested().getLevel(clampLevel(comfortLevel)).durationMinutes * 60 * 20;
    }

    public static void applyToPlayer(ServerPlayer player, int comfortLevel) {
        if (comfortLevel <= 0) return;
        int lvlIdx = clampLevel(comfortLevel);
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(lvlIdx);

        removeFromPlayer(player);

        UUID uuid = player.getUUID();
        levelMap.put(uuid, lvlIdx);
        ticksRemainingMap.put(uuid, getTotalDurationTicks(lvlIdx));

        applyHealthBonus(player, lvl.healthBonus);
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.hastePhase1, lvl.attackSpeedPhase1);

        player.setHealth(player.getMaxHealth());
    }

    public static void removeFromPlayer(ServerPlayer player) {
        UUID uuid = player.getUUID();
        levelMap.remove(uuid);
        ticksRemainingMap.remove(uuid);
        phaseMap.remove(uuid);
        mvpMap.remove(uuid);
        removeAttributes(player);
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
    }

    public static void tick(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!ticksRemainingMap.containsKey(uuid)) return;

        int ticksRemaining = ticksRemainingMap.getOrDefault(uuid, 0);
        int level = levelMap.getOrDefault(uuid, 1);

        if (ticksRemaining <= 0) {
            removeFromPlayer(player);
            return;
        }

        ticksRemaining--;
        ticksRemainingMap.put(uuid, ticksRemaining);

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

        int previousPhase = phaseMap.getOrDefault(uuid, -1);

        if (previousPhase != currentPhaseIndex) {
            phaseMap.put(uuid, currentPhaseIndex);
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

    private static void applyHealthBonus(ServerPlayer player, float bonus) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null && healthAttr.getModifier(HEALTH_ID) == null) {
            healthAttr.addPermanentModifier(new AttributeModifier(
                    HEALTH_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void applyPhaseAttributes(ServerPlayer player, float speed, float strength, float haste, float attackSpeed) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var strengthAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var hasteAttr = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        var attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null && speedAttr.getModifier(SPEED_ID) == null)
            speedAttr.addPermanentModifier(new AttributeModifier(SPEED_ID, speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (strengthAttr != null && strengthAttr.getModifier(STRENGTH_ID) == null)
            strengthAttr.addPermanentModifier(new AttributeModifier(STRENGTH_ID, strength, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (hasteAttr != null && hasteAttr.getModifier(HASTE_ID) == null)
            hasteAttr.addPermanentModifier(new AttributeModifier(HASTE_ID, haste, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (attackSpeedAttr != null && attackSpeedAttr.getModifier(ATTACK_SPEED_ID) == null)
            attackSpeedAttr.addPermanentModifier(new AttributeModifier(ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static void removePhaseAttributes(ServerPlayer player) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var strengthAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var hasteAttr = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        var attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_ID);
        if (strengthAttr != null) strengthAttr.removeModifier(STRENGTH_ID);
        if (hasteAttr != null) hasteAttr.removeModifier(HASTE_ID);
        if (attackSpeedAttr != null) attackSpeedAttr.removeModifier(ATTACK_SPEED_ID);
    }

    private static void removeAttributes(ServerPlayer player) {
        removePhaseAttributes(player);
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) healthAttr.removeModifier(HEALTH_ID);
    }

    public static boolean isMvp(ServerPlayer player) {
        return mvpMap.getOrDefault(player.getUUID(), false);
    }

    public static void applyMvpToPlayer(ServerPlayer player) {
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(5);
        int durationTicks = CONFIG.getMvp().mvpWellRestedDurationMinutes * 60 * 20;

        removeFromPlayer(player);

        UUID uuid = player.getUUID();
        levelMap.put(uuid, 5);
        ticksRemainingMap.put(uuid, durationTicks);
        mvpMap.put(uuid, true);

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
        return ticksRemainingMap.getOrDefault(player.getUUID(), 0) > 0;
    }

    public static int getTicksRemaining(ServerPlayer player) {
        return ticksRemainingMap.getOrDefault(player.getUUID(), 0);
    }

    public static int getLevel(ServerPlayer player) {
        return levelMap.getOrDefault(player.getUUID(), 0);
    }

    public static int getCurrentPhase(ServerPlayer player) {
        return phaseMap.getOrDefault(player.getUUID(), 0);
    }
}