package mt.server;

import mt.config.MidnightThoughtsConfig;
import mt.platform.MTEvents;
import mt.platform.MTPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;

public class WellRestedEffect {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static int clampLevel(int level) {
        if (level < 1) return 1;
        return Math.min(level, 5);
    }

    private static int getNbtInt(ServerPlayer player, String key, int def) {
        CompoundTag data = MTPlatform.persistentData(player);
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

        MTPlatform.persistentData(player).putInt("mt_well_rested_level", lvlIdx);
        MTPlatform.persistentData(player).putInt("mt_well_rested_ticks_remaining", getTotalDurationTicks(lvlIdx));

        WellRestedBuffs.apply(player, lvl, 0, getTotalDurationTicks(lvlIdx));

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
        MTPlatform.persistentData(player).remove("mt_well_rested_level");
        MTPlatform.persistentData(player).remove("mt_well_rested_ticks_remaining");
        MTPlatform.persistentData(player).remove("mt_well_rested_phase");
        MTPlatform.persistentData(player).remove("mt_well_rested_mvp_flag");
        WellRestedBuffs.removeAll(player);
    }

    public static void tick(ServerPlayer player) {
        if (!CONFIG.getWellRested().enabled) {
            if (MTPlatform.persistentData(player).contains("mt_well_rested_ticks_remaining")) {
                removeFromPlayer(player);
                syncAttributes(player);
                MTEvents.wellRestedExpired(player);
            }
            return;
        }
        if (!MTPlatform.persistentData(player).contains("mt_well_rested_ticks_remaining")) return;

        int ticksRemaining = getNbtInt(player, "mt_well_rested_ticks_remaining", 0);
        int level = getNbtInt(player, "mt_well_rested_level", 1);
        int totalTicks = getTotalDurationTicksForPlayer(player);

        if (ticksRemaining > totalTicks) {
            ticksRemaining = totalTicks;
        }

        if (ticksRemaining <= 0) {
            removeFromPlayer(player);
            syncAttributes(player);
            MTEvents.wellRestedExpired(player);
            return;
        }

        ticksRemaining--;
        MTPlatform.persistentData(player).putInt("mt_well_rested_ticks_remaining", ticksRemaining);

        int phaseTicks = totalTicks / 3;
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(level);

        int currentPhaseIndex = phaseTicks > 0 ? (totalTicks - ticksRemaining) / phaseTicks : 0;
        if (currentPhaseIndex > 2) currentPhaseIndex = 2;

        float regen = lvl.regenBonus;
        int previousPhase = getNbtInt(player, "mt_well_rested_phase", -1);

        if (previousPhase != currentPhaseIndex) {
            MTPlatform.persistentData(player).putInt("mt_well_rested_phase", currentPhaseIndex);
            WellRestedBuffs.apply(player, lvl, currentPhaseIndex, ticksRemaining);
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

    public static boolean isMvp(ServerPlayer player) {
        return getNbtInt(player, "mt_well_rested_mvp_flag", 0) == 1;
    }

    public static void applyMvpToPlayer(ServerPlayer player) {
        if (!CONFIG.getWellRested().enabled) return;
        MidnightThoughtsConfig.WellRestedLevel lvl = CONFIG.getWellRested().getLevel(5);
        int durationTicks = CONFIG.getMvp().mvpWellRestedDurationMinutes * 60 * 20;

        removeFromPlayer(player);

        MTPlatform.persistentData(player).putInt("mt_well_rested_level", 5);
        MTPlatform.persistentData(player).putInt("mt_well_rested_ticks_remaining", durationTicks);
        MTPlatform.persistentData(player).putInt("mt_well_rested_mvp_flag", 1);

        WellRestedBuffs.apply(player, lvl, 0, durationTicks);

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
        return MTPlatform.persistentData(player).contains("mt_well_rested_ticks_remaining")
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
