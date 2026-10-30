package mt.cache;

import mt.server.AchievementDefinition;

import java.util.Collections;
import java.util.List;

public class ClientAchievementCache {
    private static List<AchievementDefinition> cached = Collections.emptyList();

    public static void apply(List<AchievementDefinition> achievements) {
        cached = List.copyOf(achievements);
    }

    public static List<AchievementDefinition> get() {
        return cached;
    }

    public static void clear() {
        cached = Collections.emptyList();
    }
}