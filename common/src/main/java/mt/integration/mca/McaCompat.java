package mt.integration.mca;

import mt.platform.MTPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public final class McaCompat {
    private static final String MOD_ID = "mca";
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static final String CLIENT_SUFFIX = ".MCAClient";
    private static final String SKIN_CACHE_SUFFIX = ".client.render.DynamicSkinCache";

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
        if (!MTPlatform.isModLoaded(MOD_ID)) return;

        String loader = findClass("net.fabricmc.loader.api.FabricLoader") != null ? "fabric" : "forge";
        String[] packageRoots = {"net.conczin.mca", loader + ".net.conczin.mca", "net.mca"};
        for (String root : packageRoots) {
            Class<?> client = findClass(root + CLIENT_SUFFIX);
            Class<?> cache = findClass(root + SKIN_CACHE_SUFFIX);
            if (client == null || cache == null) continue;
            try {
                useVillagerRenderer = client.getMethod("useVillagerRenderer", UUID.class);
                getPlayerData = client.getMethod("getPlayerData", UUID.class);
                stitchedSkin = cache.getMethod("getOrCreateStitchedSkin", Entity.class);
                available = true;
                LOGGER.info("MCA Reborn detected in {}, summary heads will use its skins", root);
                return;
            } catch (Exception | LinkageError error) {
                disable("MCA Reborn is installed but its skin API changed, summary heads stay vanilla", error);
                return;
            }
        }
    }

    private static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException | LinkageError ignored) {
            return null;
        }
    }

    private static void disable(String message, Throwable error) {
        available = false;
        useVillagerRenderer = null;
        getPlayerData = null;
        stitchedSkin = null;
        LOGGER.warn("Midnight Thoughts {}: {}", message, error.toString());
    }
}
