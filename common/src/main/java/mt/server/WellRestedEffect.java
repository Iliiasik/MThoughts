package mt.server;

import mt.config.MidnightThoughtsConfig;
import mt.platform.MTEvents;
import mt.platform.MTPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

public class WellRestedEffect {
    private static final MidnightThoughtsConfig CONFIG = MidnightThoughtsConfig.getInstance();

    private static final String LEVEL_KEY = "mt_well_rested_level";
    private static final String TICKS_KEY = "mt_well_rested_ticks_remaining";
    private static final String PHASE_KEY = "mt_well_rested_phase";
    private static final String MVP_KEY = "mt_well_rested_mvp_flag";

    private static final int MAX_LEVEL = 5;
    private static final int PHASES = 3;
    private static final int REGEN_INTERVAL_TICKS = 100;

    public static void applyToPlayer(ServerPlayer player, int comfortLevel) {
        if (comfortLevel <= 0) return;
        int level = Math.min(comfortLevel, MAX_LEVEL);
        start(player, level, durationTicks(level), false);
    }

    public static void applyMvpToPlayer(ServerPlayer player) {
        start(player, MAX_LEVEL, mvpDurationTicks(), true);
    }

    private static void start(ServerPlayer player, int level, int durationTicks, boolean mvp) {
        if (!CONFIG.getWellRested().enabled) return;
        remove(player);

        CompoundTag data = MTPlatform.persistentData(player);
        data.putInt(LEVEL_KEY, level);
        data.putInt(TICKS_KEY, durationTicks);
        if (mvp) data.putInt(MVP_KEY, 1);

        WellRestedBuffs.apply(player, CONFIG.getWellRested().getLevel(level), 0, durationTicks);
        player.setHealth(player.getMaxHealth());
        MTEvents.wellRestedApplied(player, level, mvp, durationTicks);
    }

    public static void clear(ServerPlayer player) {
        boolean had = hasEffect(player);
        remove(player);
        if (had) MTEvents.wellRestedExpired(player);
    }

    public static void tick(ServerPlayer player) {
        if (!MTPlatform.persistentData(player).contains(TICKS_KEY)) return;

        int totalTicks = getTotalDurationTicksForPlayer(player);
        int ticksRemaining = Math.min(getTicksRemaining(player), totalTicks);
        if (!CONFIG.getWellRested().enabled || ticksRemaining <= 0) {
            expire(player);
            return;
        }

        ticksRemaining--;
        MTPlatform.persistentData(player).putInt(TICKS_KEY, ticksRemaining);

        MidnightThoughtsConfig.WellRestedLevel level = CONFIG.getWellRested().getLevel(getLevel(player));
        int phaseTicks = totalTicks / PHASES;
        int phase = phaseTicks > 0 ? Math.min((totalTicks - ticksRemaining) / phaseTicks, PHASES - 1) : 0;

        if (getInt(player, PHASE_KEY, -1) != phase) {
            MTPlatform.persistentData(player).putInt(PHASE_KEY, phase);
            WellRestedBuffs.apply(player, level, phase, ticksRemaining);
            syncAttributes(player);
        }

        if (level.regenBonus > 0 && ticksRemaining % REGEN_INTERVAL_TICKS == 0) {
            float maxHealth = player.getMaxHealth();
            if (player.getHealth() < maxHealth) {
                player.setHealth(Math.min(player.getHealth() + maxHealth * level.regenBonus * 0.5f, maxHealth));
            }
        }
    }

    private static void expire(ServerPlayer player) {
        remove(player);
        syncAttributes(player);
        MTEvents.wellRestedExpired(player);
    }

    private static void remove(ServerPlayer player) {
        CompoundTag data = MTPlatform.persistentData(player);
        data.remove(LEVEL_KEY);
        data.remove(TICKS_KEY);
        data.remove(PHASE_KEY);
        data.remove(MVP_KEY);
        WellRestedBuffs.removeAll(player);
    }

    private static void syncAttributes(ServerPlayer player) {
        var dirty = player.getAttributes().getDirtyAttributes();
        if (!dirty.isEmpty()) {
            player.connection.send(new ClientboundUpdateAttributesPacket(player.getId(), dirty));
            dirty.clear();
        }
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
    }

    private static int durationTicks(int level) {
        return minutesToTicks(CONFIG.getWellRested().getLevel(level).durationMinutes);
    }

    private static int mvpDurationTicks() {
        return minutesToTicks(CONFIG.getMvp().mvpWellRestedDurationMinutes);
    }

    private static int minutesToTicks(int minutes) {
        return minutes * 60 * 20;
    }

    private static int getInt(ServerPlayer player, String key, int fallback) {
        CompoundTag data = MTPlatform.persistentData(player);
        return data.contains(key) ? data.getInt(key) : fallback;
    }

    public static int getTotalDurationTicksForPlayer(ServerPlayer player) {
        return isMvp(player) ? mvpDurationTicks() : durationTicks(Mth.clamp(getLevel(player), 1, MAX_LEVEL));
    }

    public static boolean hasEffect(ServerPlayer player) {
        return getTicksRemaining(player) > 0;
    }

    public static boolean isMvp(ServerPlayer player) {
        return getInt(player, MVP_KEY, 0) == 1;
    }

    public static int getTicksRemaining(ServerPlayer player) {
        return getInt(player, TICKS_KEY, 0);
    }

    public static int getLevel(ServerPlayer player) {
        return getInt(player, LEVEL_KEY, 0);
    }

    public static int getCurrentPhase(ServerPlayer player) {
        return getInt(player, PHASE_KEY, 0);
    }
}
