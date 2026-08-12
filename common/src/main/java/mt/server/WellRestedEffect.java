package mt.server;

import mt.config.MidnightThoughtsConfig;
import mt.platform.MTEvents;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WellRestedEffect {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final Identifier SPEED_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_speed");
    private static final Identifier STRENGTH_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_strength");
    private static final Identifier ATTACK_SPEED_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_attack_speed");
    private static final Identifier HASTE_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_haste");
    private static final Identifier HEALTH_ID = Identifier.fromNamespaceAndPath("midnightthoughts", "well_rested_health");

    private static int clampLevel(int level) {
        if (level < 1) return 1;
        return Math.min(level, 5);
    }

    @SuppressWarnings("resource")
    private static WellRestedData data(ServerPlayer player) {
        return WellRestedData.get(player.level().getServer());
    }

    private static WellRestedData.Entry entry(ServerPlayer player) {
        return data(player).get(player.getUUID());
    }

    private static void store(ServerPlayer player, WellRestedData.Entry value) {
        data(player).put(player.getUUID(), value);
    }

    public static int getTotalDurationTicks(int comfortLevel) {
        return CONFIG.getWellRested().getLevel(clampLevel(comfortLevel)).durationMinutes * 60 * 20;
    }

    public static void applyToPlayer(ServerPlayer player, int comfortLevel) {
        if (!CONFIG.getWellRested().enabled) return;
        if (comfortLevel <= 0) return;
        int lvlIdx = clampLevel(comfortLevel);
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(lvlIdx);

        removeFromPlayer(player);

        store(player, new WellRestedData.Entry(lvlIdx, getTotalDurationTicks(lvlIdx), -1, false));

        applyHealthBonus(player, lvl.healthBonus);
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.hastePhase1, lvl.attackSpeedPhase1);

        player.setHealth(player.getMaxHealth());

        MTEvents.wellRestedApplied(player, lvlIdx, false, getTotalDurationTicks(lvlIdx));
    }

    public static void clear(ServerPlayer player) {
        boolean had = hasEffect(player);
        removeFromPlayer(player);
        if (had) {
            MTEvents.wellRestedExpired(player);
        }
    }

    public static void removeFromPlayer(ServerPlayer player) {
        data(player).remove(player.getUUID());
        removeAttributes(player);
    }

    public static void tick(ServerPlayer player) {
        WellRestedData.Entry current = entry(player);

        if (!CONFIG.getWellRested().enabled) {
            if (current != null) {
                removeFromPlayer(player);
                syncAttributes(player);
                MTEvents.wellRestedExpired(player);
            }
            return;
        }
        if (current == null) return;

        int ticksRemaining = current.ticksRemaining();
        int level = current.level();

        if (ticksRemaining <= 0) {
            removeFromPlayer(player);
            syncAttributes(player);
            MTEvents.wellRestedExpired(player);
            return;
        }

        ticksRemaining--;

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

        int previousPhase = current.phase();
        store(player, new WellRestedData.Entry(level, ticksRemaining, currentPhaseIndex, current.mvp()));

        if (previousPhase != currentPhaseIndex) {
            applyPhaseAttributes(player, speed, strength, haste, attackSpeed);
            syncAttributes(player);
        }

        if (regen > 0 && ticksRemaining % 100 == 0) {
            float maxHealth = player.getMaxHealth();
            float currentHealth = player.getHealth();
            if (currentHealth < maxHealth) {
                player.setHealth(Math.min(currentHealth + maxHealth * regen * 0.5f, maxHealth));
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
            healthAttr.addOrReplacePermanentModifier(
                    new AttributeModifier(HEALTH_ID, bonus, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void applyPhaseAttributes(ServerPlayer player, float speed, float strength, float haste, float attackSpeed) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var strengthAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var hasteAttr = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        var attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null)
            speedAttr.addOrReplacePermanentModifier(new AttributeModifier(SPEED_ID, speed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (strengthAttr != null)
            strengthAttr.addOrReplacePermanentModifier(new AttributeModifier(STRENGTH_ID, strength, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (hasteAttr != null)
            hasteAttr.addOrReplacePermanentModifier(new AttributeModifier(HASTE_ID, haste, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        if (attackSpeedAttr != null)
            attackSpeedAttr.addOrReplacePermanentModifier(new AttributeModifier(ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
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
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
    }

    public static boolean isMvp(ServerPlayer player) {
        WellRestedData.Entry e = entry(player);
        return e != null && e.mvp();
    }

    public static void applyMvpToPlayer(ServerPlayer player) {
        if (!CONFIG.getWellRested().enabled) return;
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(5);
        int durationTicks = CONFIG.getMvp().mvpWellRestedDurationMinutes * 60 * 20;

        removeFromPlayer(player);

        store(player, new WellRestedData.Entry(5, durationTicks, -1, true));

        applyHealthBonus(player, lvl.healthBonus);
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.hastePhase1, lvl.attackSpeedPhase1);

        player.setHealth(player.getMaxHealth());

        MTEvents.wellRestedApplied(player, 5, true, durationTicks);
    }

    public static int getTotalDurationTicksForPlayer(ServerPlayer player) {
        if (isMvp(player)) {
            return CONFIG.getMvp().mvpWellRestedDurationMinutes * 60 * 20;
        }
        return getTotalDurationTicks(getLevel(player));
    }

    public static boolean hasEffect(ServerPlayer player) {
        WellRestedData.Entry e = entry(player);
        return e != null && e.ticksRemaining() > 0;
    }

    public static int getTicksRemaining(ServerPlayer player) {
        WellRestedData.Entry e = entry(player);
        return e != null ? e.ticksRemaining() : 0;
    }

    public static int getLevel(ServerPlayer player) {
        WellRestedData.Entry e = entry(player);
        return e != null ? e.level() : 0;
    }

    public static int getCurrentPhase(ServerPlayer player) {
        WellRestedData.Entry e = entry(player);
        return e != null ? Math.max(0, e.phase()) : 0;
    }
}
