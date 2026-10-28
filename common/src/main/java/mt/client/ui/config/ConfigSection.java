package mt.client.ui.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum ConfigSection {
    SLEEP_OVERLAY("sleep_overlay"),
    INTERFACE("interface"),
    MVP("mvp"),
    COMFORT("comfort"),
    SERVER("server");

    private final String key;

    ConfigSection(String key) {
        this.key = key;
    }

    public MutableComponent title() {
        return Component.translatable("screen.midnightthoughts.config." + key);
    }
}
