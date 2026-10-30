package mt;

import net.minecraft.resources.ResourceLocation;

public final class MTConstants {
    public static final String MOD_ID = "midnightthoughts";

    private MTConstants() {}

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
