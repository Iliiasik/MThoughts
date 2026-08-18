package mt.server;

import mt.common.PlayerData;
import mt.config.MidnightThoughtsConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class WellRestedEffect {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final UUID SPEED_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID STRENGTH_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    private static final UUID HEALTH_UUID = UUID.fromString("e5f6a7b8-c9d0-1234-efab-345678901234");

    private static int clampLevel(int level) {
        if (level < 1) return 1;
        return Math.min(level, 5);
    }

    private static int getNbtInt(ServerPlayer player, String key, int def) {
        CompoundTag data = PlayerData.of(player);
        return data.contains(key) ? data.getInt(key) : def;
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

        PlayerData.of(player).putInt("mt_well_rested_level", lvlIdx);
        PlayerData.of(player).putInt("mt_well_rested_ticks_remaining", getTotalDurationTicks(lvlIdx));

        applyHealthBonus(player, lvl.healthBonus);
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.attackSpeedPhase1);

        player.setHealth(player.getMaxHealth());

        mt.api.event.WellRestedAppliedCallback.EVENT.invoker().onWellRestedApplied(player, lvlIdx, false, getTotalDurationTicks(lvlIdx));
    }

    public static void clear(ServerPlayer player) {
        boolean had = hasEffect(player);
        removeFromPlayer(player);
        if (had) {
            mt.api.event.WellRestedExpiredCallback.EVENT.invoker().onWellRestedExpired(player);
        }
    }

    public static void removeFromPlayer(ServerPlayer player) {
        PlayerData.of(player).remove("mt_well_rested_level");
        PlayerData.of(player).remove("mt_well_rested_ticks_remaining");
        PlayerData.of(player).remove("mt_well_rested_phase");
        PlayerData.of(player).remove("mt_well_rested_mvp_flag");
        removeAttributes(player);
    }

    public static void tick(ServerPlayer player) {
        if (!CONFIG.getWellRested().enabled) {
            if (PlayerData.of(player).contains("mt_well_rested_ticks_remaining")) {
                removeFromPlayer(player);
                syncAttributes(player);
                mt.api.event.WellRestedExpiredCallback.EVENT.invoker().onWellRestedExpired(player);
            }
            return;
        }
        if (!PlayerData.of(player).contains("mt_well_rested_ticks_remaining")) return;

        int ticksRemaining = getNbtInt(player, "mt_well_rested_ticks_remaining", 0);
        int level = getNbtInt(player, "mt_well_rested_level", 1);
        int totalTicks = getTotalDurationTicksForPlayer(player);

        if (ticksRemaining > totalTicks) {
            ticksRemaining = totalTicks;
        }

        if (ticksRemaining <= 0) {
            removeFromPlayer(player);
            syncAttributes(player);
            mt.api.event.WellRestedExpiredCallback.EVENT.invoker().onWellRestedExpired(player);
            return;
        }

        ticksRemaining--;
        PlayerData.of(player).putInt("mt_well_rested_ticks_remaining", ticksRemaining);

        int phaseTicks = totalTicks / 3;
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(level);

        int currentPhaseIndex = phaseTicks > 0 ? (totalTicks - ticksRemaining) / phaseTicks : 0;
        if (currentPhaseIndex > 2) currentPhaseIndex = 2;

        float regen = lvl.regenBonus;

        float speed, strength, attackSpeed;
        switch (currentPhaseIndex) {
            case 0 -> { speed = lvl.speedPhase1; strength = lvl.strengthPhase1; attackSpeed = lvl.attackSpeedPhase1; }
            case 1 -> { speed = lvl.speedPhase2; strength = lvl.strengthPhase2; attackSpeed = lvl.attackSpeedPhase2; }
            default -> { speed = lvl.speedPhase3; strength = lvl.strengthPhase3; attackSpeed = lvl.attackSpeedPhase3; }
        }

        int previousPhase = getNbtInt(player, "mt_well_rested_phase", -1);

        if (previousPhase != currentPhaseIndex) {
            PlayerData.of(player).putInt("mt_well_rested_phase", currentPhaseIndex);
            removePhaseAttributes(player);
            applyPhaseAttributes(player, speed, strength, attackSpeed);
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
        var dirty = player.getAttributes().getDirtyAttributes();
        if (!dirty.isEmpty()) {
            player.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), dirty));
        }
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
    }

    private static void applyHealthBonus(ServerPlayer player, float bonus) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null && healthAttr.getModifier(HEALTH_UUID) == null) {
            healthAttr.addPermanentModifier(new AttributeModifier(
                    HEALTH_UUID, "well_rested_health", bonus, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void applyPhaseAttributes(ServerPlayer player, float speed, float strength, float attackSpeed) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var strengthAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null && speedAttr.getModifier(SPEED_UUID) == null)
            speedAttr.addPermanentModifier(new AttributeModifier(SPEED_UUID, "well_rested_speed", speed, AttributeModifier.Operation.MULTIPLY_BASE));
        if (strengthAttr != null && strengthAttr.getModifier(STRENGTH_UUID) == null)
            strengthAttr.addPermanentModifier(new AttributeModifier(STRENGTH_UUID, "well_rested_strength", strength, AttributeModifier.Operation.ADDITION));
        if (attackSpeedAttr != null && attackSpeedAttr.getModifier(ATTACK_SPEED_UUID) == null)
            attackSpeedAttr.addPermanentModifier(new AttributeModifier(ATTACK_SPEED_UUID, "well_rested_attack_speed", attackSpeed, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    private static void removePhaseAttributes(ServerPlayer player) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        var strengthAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        var attackSpeedAttr = player.getAttribute(Attributes.ATTACK_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(SPEED_UUID);
        if (strengthAttr != null) strengthAttr.removeModifier(STRENGTH_UUID);
        if (attackSpeedAttr != null) attackSpeedAttr.removeModifier(ATTACK_SPEED_UUID);
    }

    private static void removeAttributes(ServerPlayer player) {
        removePhaseAttributes(player);
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) healthAttr.removeModifier(HEALTH_UUID);
    }

    public static boolean isMvp(ServerPlayer player) {
        return getNbtInt(player, "mt_well_rested_mvp_flag", 0) == 1;
    }

    public static void applyMvpToPlayer(ServerPlayer player) {
        if (!CONFIG.getWellRested().enabled) return;
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(5);
        int durationTicks = CONFIG.getMvp().mvpWellRestedDurationMinutes * 60 * 20;

        removeFromPlayer(player);

        PlayerData.of(player).putInt("mt_well_rested_level", 5);
        PlayerData.of(player).putInt("mt_well_rested_ticks_remaining", durationTicks);
        PlayerData.of(player).putInt("mt_well_rested_mvp_flag", 1);

        applyHealthBonus(player, lvl.healthBonus);
        applyPhaseAttributes(player, lvl.speedPhase1, lvl.strengthPhase1, lvl.attackSpeedPhase1);

        player.setHealth(player.getMaxHealth());

        mt.api.event.WellRestedAppliedCallback.EVENT.invoker().onWellRestedApplied(player, 5, true, durationTicks);
    }

    public static int getTotalDurationTicksForPlayer(ServerPlayer player) {
        if (isMvp(player)) {
            return CONFIG.getMvp().mvpWellRestedDurationMinutes * 60 * 20;
        }
        return getTotalDurationTicks(getLevel(player));
    }

    public static boolean hasEffect(ServerPlayer player) {
        return PlayerData.of(player).contains("mt_well_rested_ticks_remaining")
                && getNbtInt(player, "mt_well_rested_ticks_remaining", 0) > 0;
    }

    public static int getTicksRemaining(ServerPlayer player) {
        return getNbtInt(player, "mt_well_rested_ticks_remaining", 0);
    }

    public static int getLevel(ServerPlayer player) {
        return getNbtInt(player, "mt_well_rested_level", 0);
    }

    public static int getCurrentPhase(ServerPlayer player) {
        return getNbtInt(player, "mt_well_rested_phase", 0);
    }
}