package mt.client.ui.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.List;

public enum ConfigSection {
    SLEEP_OVERLAY("sleep_overlay"),
    INTERFACE("interface"),
    MVP("mvp"),
    COMFORT("comfort"),
    SERVER("server"),
    PERSONAL("personal");

    private final String key;

    ConfigSection(String key) {
        this.key = key;
    }

    public static List<ConfigSection> shown(boolean editable) {
        if (!editable) return List.of(PERSONAL);
        return Arrays.stream(values()).filter(section -> section != PERSONAL).toList();
    }

    public MutableComponent title() {
        return Component.translatable("screen.midnightthoughts.config." + key);
    }
}
