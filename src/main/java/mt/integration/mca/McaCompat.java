package mt.integration.mca;

import mt.MidnightThoughts;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public final class McaCompat {
    private static final String MOD_ID = "mca";

    private static final String[] CLIENT_CLASSES = {
            "net.conczin.mca.MCAClient",
            "net.mca.MCAClient"
    };
    private static final String[] SKIN_CACHE_CLASSES = {
            "net.conczin.mca.client.render.DynamicSkinCache",
            "net.mca.client.render.DynamicSkinCache"
    };

    private static boolean resolved;
    private static boolean available;
    private static Method useVillagerRenderer;
    private static Method getPlayerData;
    private static Method stitchedSkin;

    private McaCompat() {}

    public static void prewarm(UUID uuid) {
        headTexture(uuid);
    }

    public static ResourceLocation headTexture(UUID uuid) {
        if (uuid == null) return null;
        if (!resolved) resolve();
        if (!available) return null;

        try {
            if (!Boolean.TRUE.equals(useVillagerRenderer.invoke(null, uuid))) return null;

            Object result = getPlayerData.invoke(null, uuid);
            if (!(result instanceof Optional<?> data)) return null;

            Object villager = data.orElse(null);
            if (!(villager instanceof Entity entity)) return null;

            Object skin = stitchedSkin.invoke(null, entity);
            return skin instanceof ResourceLocation texture ? texture : null;
        } catch (Exception | LinkageError error) {
            disable("could not read an MCA Reborn skin, falling back to vanilla heads", error);
            return null;
        }
    }

    private static void resolve() {
        resolved = true;
        if (!ModList.get().isLoaded(MOD_ID)) return;

        try {
            Class<?> client = findClass(CLIENT_CLASSES);
            Class<?> cache = findClass(SKIN_CACHE_CLASSES);
            if (client == null || cache == null) return;

            useVillagerRenderer = client.getMethod("useVillagerRenderer", UUID.class);
            getPlayerData = client.getMethod("getPlayerData", UUID.class);
            stitchedSkin = cache.getMethod("getOrCreateStitchedSkin", Entity.class);
            available = true;
            MidnightThoughts.LOGGER.info("MCA Reborn detected, summary heads will use its skins");
        } catch (Exception | LinkageError error) {
            disable("MCA Reborn is installed but its skin API changed, summary heads stay vanilla", error);
        }
    }

    private static Class<?> findClass(String[] names) {
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        return null;
    }

    private static void disable(String message, Throwable error) {
        available = false;
        useVillagerRenderer = null;
        getPlayerData = null;
        stitchedSkin = null;
        MidnightThoughts.LOGGER.warn("Midnight Thoughts {}: {}", message, error.toString());
    }
}
