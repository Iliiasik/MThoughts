package mt.server;

import mt.config.MidnightThoughtsConfig;
import mt.platform.MTPlatform;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class WellRestedBuffs {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static final String ATTRIBUTES_KEY = "mt_well_rested_attributes";
    private static final String EFFECTS_KEY = "mt_well_rested_effects";
    private static final String SEPARATOR = ";";
    private static final String MODIFIER_PREFIX = "midnightthoughts:well_rested/";
    private static final Set<String> REPORTED_MISSING = new HashSet<>();

    private WellRestedBuffs() {}

    public static void apply(ServerPlayer player, MidnightThoughtsConfig.WellRestedLevel level,
                             int phaseIndex, int ticksRemaining) {
        applyAttributes(player, level.attributes, phaseIndex);
        applyEffects(player, level.effects, phaseIndex, ticksRemaining);
    }

    public static void removeAll(ServerPlayer player) {
        storedIds(player, ATTRIBUTES_KEY).forEach(id -> removeAttribute(player, id));
        storedIds(player, EFFECTS_KEY).forEach(id -> removeEffect(player, id));
        MTPlatform.persistentData(player).remove(ATTRIBUTES_KEY);
        MTPlatform.persistentData(player).remove(EFFECTS_KEY);
    }

    public static void clearCache() {
        REPORTED_MISSING.clear();
    }

    private static void applyAttributes(ServerPlayer player,
                                        List<MidnightThoughtsConfig.AttributeBonus> bonuses, int phaseIndex) {
        Set<String> previous = storedIds(player, ATTRIBUTES_KEY);
        Set<String> current = new LinkedHashSet<>();

        for (MidnightThoughtsConfig.AttributeBonus bonus : bonuses) {
            Holder.Reference<Attribute> attribute = resolve(BuiltInRegistries.ATTRIBUTE, bonus.id);
            if (attribute == null) continue;
            String canonical = attribute.key().location().toString();
            if (current.contains(canonical)) continue;
            AttributeInstance instance = player.getAttribute(attribute.value());
            if (instance == null) continue;
            UUID id = modifierId(canonical);
            instance.removeModifier(id);
            instance.addPermanentModifier(new AttributeModifier(
                    id, MODIFIER_PREFIX + canonical, phaseValue(bonus, phaseIndex), operationOf(bonus.operation)));
            current.add(canonical);
        }

        for (String id : previous) {
            if (!current.contains(id)) removeAttribute(player, id);
        }

        store(player, ATTRIBUTES_KEY, current);
    }

    private static void applyEffects(ServerPlayer player,
                                     List<MidnightThoughtsConfig.EffectBonus> bonuses,
                                     int phaseIndex, int ticksRemaining) {
        Set<String> previous = storedIds(player, EFFECTS_KEY);
        Set<String> seen = new LinkedHashSet<>();
        Set<String> current = new LinkedHashSet<>();
        int duration = Math.max(1, ticksRemaining);

        for (MidnightThoughtsConfig.EffectBonus bonus : bonuses) {
            Holder.Reference<MobEffect> effect = resolve(BuiltInRegistries.MOB_EFFECT, bonus.id);
            if (effect == null) continue;
            String canonical = effect.key().location().toString();
            if (!seen.add(canonical)) continue;

            int amplifier = phaseAmplifier(bonus, phaseIndex);
            if (amplifier < 0) continue;
            if (previous.contains(canonical)) player.removeEffect(effect.value());
            player.addEffect(new MobEffectInstance(effect.value(), duration, amplifier, true, false, true));
            current.add(canonical);
        }

        for (String id : previous) {
            if (!current.contains(id)) removeEffect(player, id);
        }

        store(player, EFFECTS_KEY, current);
    }

    private static double phaseValue(MidnightThoughtsConfig.AttributeBonus bonus, int phaseIndex) {
        return switch (phaseIndex) {
            case 0 -> bonus.phase1;
            case 1 -> bonus.phase2;
            default -> bonus.phase3;
        };
    }

    private static int phaseAmplifier(MidnightThoughtsConfig.EffectBonus bonus, int phaseIndex) {
        return switch (phaseIndex) {
            case 0 -> bonus.phase1;
            case 1 -> bonus.phase2;
            default -> bonus.phase3;
        };
    }

    private static void removeAttribute(ServerPlayer player, String id) {
        Holder.Reference<Attribute> attribute = resolve(BuiltInRegistries.ATTRIBUTE, id);
        if (attribute == null) return;
        AttributeInstance instance = player.getAttribute(attribute.value());
        if (instance != null) instance.removeModifier(modifierId(id));
    }

    private static void removeEffect(ServerPlayer player, String id) {
        Holder.Reference<MobEffect> effect = resolve(BuiltInRegistries.MOB_EFFECT, id);
        if (effect != null) player.removeEffect(effect.value());
    }

    private static AttributeModifier.Operation operationOf(String operation) {
        return switch (operation) {
            case "add_multiplied_base", "multiply_base" -> AttributeModifier.Operation.MULTIPLY_BASE;
            case "add_multiplied_total", "multiply_total" -> AttributeModifier.Operation.MULTIPLY_TOTAL;
            default -> AttributeModifier.Operation.ADDITION;
        };
    }

    private static <T> Holder.Reference<T> resolve(Registry<T> registry, String rawId) {
        ResourceLocation location = ResourceLocation.tryParse(rawId);
        Optional<Holder.Reference<T>> holder = location == null
                ? Optional.empty()
                : registry.getHolder(ResourceKey.create(registry.key(), location));
        if (holder.isEmpty()) {
            warnMissing(rawId);
            return null;
        }
        return holder.get();
    }

    private static void warnMissing(String rawId) {
        if (REPORTED_MISSING.add(rawId)) {
            LOGGER.warn("Well Rested bonus references unknown id '{}', it will be skipped", rawId);
        }
    }

    private static UUID modifierId(String rawId) {
        return UUID.nameUUIDFromBytes((MODIFIER_PREFIX + rawId).getBytes(StandardCharsets.UTF_8));
    }

    private static Set<String> storedIds(ServerPlayer player, String key) {
        CompoundTag data = MTPlatform.persistentData(player);
        if (!data.contains(key)) return Set.of();
        Set<String> ids = new LinkedHashSet<>();
        for (String id : data.getString(key).split(SEPARATOR)) {
            if (!id.isEmpty()) ids.add(id);
        }
        return ids;
    }

    private static void store(ServerPlayer player, String key, Set<String> ids) {
        if (ids.isEmpty()) {
            MTPlatform.persistentData(player).remove(key);
            return;
        }
        MTPlatform.persistentData(player).putString(key, String.join(SEPARATOR, ids));
    }
}
