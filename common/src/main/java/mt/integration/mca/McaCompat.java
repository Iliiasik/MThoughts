package mt.integration.mca;

import mt.platform.MTPlatform;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public final class McaCompat {
    private static final String MOD_ID = "mca";
    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static final String CLIENT_CLASS = "net.conczin.mca.MCAClient";
    private static final String VILLAGER_CLASS = "net.conczin.mca.entity.VillagerLike";
    private static final String VISUALS_CLASS = "net.conczin.mca.client.render.VillagerVisuals";
    private static final String SKIN_CACHE_CLASS = "net.conczin.mca.client.render.DynamicSkinCache";

    private static boolean resolved;
    private static boolean available;
    private static Class<?> villagerLike;
    private static Method useVillagerRenderer;
    private static Method getPlayerData;
    private static Method captureVisuals;
    private static Method stitchedSkin;

    private McaCompat() {}

    public static void prewarm(UUID uuid) {
        headTexture(uuid);
    }

    public static Identifier headTexture(UUID uuid) {
        if (uuid == null) return null;
        if (!resolved) resolve();
        if (!available) return null;

        try {
            if (!Boolean.TRUE.equals(useVillagerRenderer.invoke(null, uuid))) return null;

            Object result = getPlayerData.invoke(null, uuid);
            if (!(result instanceof Optional<?> data)) return null;

            Object villager = data.orElse(null);
            if (!villagerLike.isInstance(villager)) return null;

            Object skin = stitchedSkin.invoke(null, captureVisuals.invoke(null, villager));
            return skin instanceof Identifier texture ? texture : null;
        } catch (Exception | LinkageError error) {
            disable("could not read an MCA Reborn skin, falling back to vanilla heads", error);
            return null;
        }
    }

    private static void resolve() {
        resolved = true;
        if (!MTPlatform.isModLoaded(MOD_ID)) return;

        try {
            Class<?> client = Class.forName(CLIENT_CLASS);
            Class<?> visuals = Class.forName(VISUALS_CLASS);
            villagerLike = Class.forName(VILLAGER_CLASS);
            useVillagerRenderer = client.getMethod("useVillagerRenderer", UUID.class);
            getPlayerData = client.getMethod("getPlayerData", UUID.class);
            captureVisuals = visuals.getMethod("capture", villagerLike);
            stitchedSkin = Class.forName(SKIN_CACHE_CLASS).getMethod("getOrCreateStitchedSkin", visuals);
            available = true;
            LOGGER.info("MCA Reborn detected, summary heads will use its skins");
        } catch (Exception | LinkageError error) {
            disable("MCA Reborn is installed but its skin API changed, summary heads stay vanilla", error);
        }
    }

    private static void disable(String message, Throwable error) {
        available = false;
        LOGGER.warn("Midnight Thoughts {}: {}", message, error.toString());
    }
}
