package mt.server;

import mt.config.MidnightThoughtsConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class WellRestedBuffs {
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static final String ATTRIBUTES_KEY = "mt_well_rested_attributes";
    private static final String EFFECTS_KEY = "mt_well_rested_effects";
    private static final String SEPARATOR = ";";
    private static final String GENERIC_PREFIX = "generic.";
    private static final Set<String> REPORTED_MISSING = new HashSet<>();

    private WellRestedBuffs() {}

    public static void apply(ServerPlayer player, MidnightThoughtsConfig.WellRestedLevel level,
                             int phaseIndex, int ticksRemaining) {
        applyAttributes(player, level.attributes, phaseIndex);
        applyEffects(player, level.effects, phaseIndex, ticksRemaining);
    }

    public static void removeAll(ServerPlayer player) {
        for (String id : storedIds(player, ATTRIBUTES_KEY)) {
            AttributeInstance instance = attributeInstance(player, id);
            if (instance != null) instance.removeModifier(modifierId(id));
        }
        for (String id : storedIds(player, EFFECTS_KEY)) {
            Holder<MobEffect> effect = resolveEffect(id);
            if (effect != null) player.removeEffect(effect);
        }
        player.getPersistentData().remove(ATTRIBUTES_KEY);
        player.getPersistentData().remove(EFFECTS_KEY);
    }

    public static void clearCache() {
        REPORTED_MISSING.clear();
    }

    private static void applyAttributes(ServerPlayer player,
                                        List<MidnightThoughtsConfig.AttributeBonus> bonuses, int phaseIndex) {
        Set<String> previous = storedIds(player, ATTRIBUTES_KEY);
        Set<String> current = new LinkedHashSet<>();

        for (MidnightThoughtsConfig.AttributeBonus bonus : bonuses) {
            AttributeInstance instance = attributeInstance(player, bonus.id);
            if (instance == null) continue;
            instance.addOrReplacePermanentModifier(new AttributeModifier(
                    modifierId(bonus.id), phaseValue(bonus, phaseIndex), operationOf(bonus.operation)));
            current.add(bonus.id);
        }

        for (String id : previous) {
            if (current.contains(id)) continue;
            AttributeInstance instance = attributeInstance(player, id);
            if (instance != null) instance.removeModifier(modifierId(id));
        }

        store(player, ATTRIBUTES_KEY, current);
    }

    private static void applyEffects(ServerPlayer player,
                                     List<MidnightThoughtsConfig.EffectBonus> bonuses,
                                     int phaseIndex, int ticksRemaining) {
        Set<String> previous = storedIds(player, EFFECTS_KEY);
        Set<String> current = new LinkedHashSet<>();
        int duration = Math.max(1, ticksRemaining);

        for (MidnightThoughtsConfig.EffectBonus bonus : bonuses) {
            int amplifier = phaseAmplifier(bonus, phaseIndex);
            if (amplifier < 0) continue;
            Holder<MobEffect> effect = resolveEffect(bonus.id);
            if (effect == null) continue;
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, true));
            current.add(bonus.id);
        }

        for (String id : previous) {
            if (current.contains(id)) continue;
            Holder<MobEffect> effect = resolveEffect(id);
            if (effect != null) player.removeEffect(effect);
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

    private static AttributeModifier.Operation operationOf(String raw) {
        String key = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
        return switch (key) {
            case "add_multiplied_base", "multiply_base" -> AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
            case "add_multiplied_total", "multiply_total" -> AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
            default -> AttributeModifier.Operation.ADD_VALUE;
        };
    }

    private static AttributeInstance attributeInstance(ServerPlayer player, String rawId) {
        Holder<Attribute> attribute = resolveAttribute(rawId);
        return attribute == null ? null : player.getAttribute(attribute);
    }

    private static Holder<Attribute> resolveAttribute(String rawId) {
        ResourceLocation location = ResourceLocation.tryParse(rawId);
        if (location == null) {
            warnMissing(rawId);
            return null;
        }
        Holder<Attribute> direct = lookupAttribute(location);
        if (direct != null) return direct;
        Holder<Attribute> alias = lookupAttribute(withGenericToggled(location));
        if (alias != null) return alias;
        warnMissing(rawId);
        return null;
    }

    private static Holder<Attribute> lookupAttribute(ResourceLocation location) {
        if (location == null) return null;
        Optional<Holder.Reference<Attribute>> holder =
                BuiltInRegistries.ATTRIBUTE.getHolder(ResourceKey.create(Registries.ATTRIBUTE, location));
        return holder.orElse(null);
    }

    private static ResourceLocation withGenericToggled(ResourceLocation location) {
        String path = location.getPath();
        String toggled = path.startsWith(GENERIC_PREFIX)
                ? path.substring(GENERIC_PREFIX.length())
                : GENERIC_PREFIX + path;
        return ResourceLocation.tryBuild(location.getNamespace(), toggled);
    }

    private static Holder<MobEffect> resolveEffect(String rawId) {
        ResourceLocation location = ResourceLocation.tryParse(rawId);
        if (location == null) {
            warnMissing(rawId);
            return null;
        }
        Optional<Holder.Reference<MobEffect>> holder =
                BuiltInRegistries.MOB_EFFECT.getHolder(ResourceKey.create(Registries.MOB_EFFECT, location));
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

    private static ResourceLocation modifierId(String rawId) {
        StringBuilder path = new StringBuilder("well_rested/");
        for (int i = 0; i < rawId.length(); i++) {
            char c = Character.toLowerCase(rawId.charAt(i));
            boolean allowed = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
                    || c == '_' || c == '.' || c == '-' || c == '/';
            path.append(allowed ? c : '_');
        }
        return ResourceLocation.fromNamespaceAndPath("midnightthoughts", path.toString());
    }

    private static Set<String> storedIds(ServerPlayer player, String key) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(key)) return Set.of();
        Set<String> ids = new LinkedHashSet<>();
        for (String id : data.getString(key).split(SEPARATOR)) {
            if (!id.isEmpty()) ids.add(id);
        }
        return ids;
    }

    private static void store(ServerPlayer player, String key, Set<String> ids) {
        if (ids.isEmpty()) {
            player.getPersistentData().remove(key);
            return;
        }
        player.getPersistentData().putString(key, String.join(SEPARATOR, ids));
    }
}
