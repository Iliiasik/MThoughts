package mt.gametest;

import mt.config.MidnightThoughtsConfig;
import mt.server.AchievementLoader;
import mt.server.ComfortCalculator;
import mt.server.UserContentInitializer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("resource")
public final class TestHarness {

    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");

    private static String loaderName = "unknown loader";
    private static TestContext current;
    private static Map<Path, String> savedFiles = Map.of();
    private static boolean installed;

    private TestHarness() {}

    public static void setLoaderName(String name) {
        loaderName = name;
    }

    static String loaderName() {
        return loaderName;
    }

    static boolean isInstalled() {
        return installed;
    }

    static void run(GameTestHelper helper, GameTestCase testCase) {
        if (!installed) {
            installed = true;
            PacketLog.install();
            GlobalTestReporter.replaceWith(new MTTestReporter());
        }
        finishCurrent();
        savedFiles = snapshotFiles();
        TestContext context = new TestContext(helper);
        current = context;
        testCase.body().accept(context);
        context.start();
    }

    static long currentTicks() {
        return current == null ? -1L : current.lastTick();
    }

    static void finishCurrent() {
        TestContext context = current;
        if (context == null) return;
        current = null;

        for (MockPlayer player : context.players()) {
            try {
                player.leave();
            } catch (RuntimeException exception) {
                LOGGER.warn("Could not disconnect the test player {}: {}", player.name(), exception.toString());
            }
        }

        restoreFiles(savedFiles);
        MidnightThoughtsConfig.reload();
        AchievementLoader.reload();
        UserContentInitializer.invalidateCache();
        ComfortCalculator.clearCache();

        ServerLevel level = context.level();
        GameRules rules = level.getGameRules();
        rules.getRule(GameRules.RULE_DAYLIGHT).set(true, level.getServer());
        rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, level.getServer());
        rules.getRule(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE).set(100, level.getServer());
        context.clearWeather();
        context.day();

        ApiEventLog.reset();
        PacketLog.clear();
    }

    private static Map<Path, String> snapshotFiles() {
        Map<Path, String> files = new HashMap<>();
        Path dir = MidnightThoughtsConfig.getConfigDir();
        if (!Files.isDirectory(dir)) return files;
        try (Stream<Path> walk = Files.walk(dir)) {
            for (Path path : walk.filter(Files::isRegularFile).toList()) {
                files.put(path, Files.readString(path, StandardCharsets.UTF_8));
            }
        } catch (IOException exception) {
            LOGGER.warn("Could not snapshot the config folder: {}", exception.toString());
        }
        return files;
    }

    private static void restoreFiles(Map<Path, String> files) {
        Path dir = MidnightThoughtsConfig.getConfigDir();
        try {
            if (Files.isDirectory(dir)) {
                List<Path> present = new ArrayList<>();
                try (Stream<Path> walk = Files.walk(dir)) {
                    walk.filter(Files::isRegularFile).forEach(present::add);
                }
                for (Path path : present) {
                    if (!files.containsKey(path)) Files.deleteIfExists(path);
                }
            }
            for (Map.Entry<Path, String> entry : files.entrySet()) {
                Files.createDirectories(entry.getKey().getParent());
                Files.writeString(entry.getKey(), entry.getValue(), StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            LOGGER.warn("Could not restore the config folder: {}", exception.toString());
        }
    }
}
