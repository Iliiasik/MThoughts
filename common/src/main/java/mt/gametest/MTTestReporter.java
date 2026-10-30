package mt.gametest;

import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.TestReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class MTTestReporter implements TestReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger("MidnightThoughts");
    private static final String REPORT_FILE = "midnightthoughts-gametests.txt";
    private static final int NAME_WIDTH = 66;

    private record Result(String id, boolean passed, long ticks, String error) {}

    private final Map<String, Result> results = new LinkedHashMap<>();
    private final long startedAt = System.currentTimeMillis();

    @Override
    public void onTestFailed(GameTestInfo info) {
        Throwable error = info.getError();
        String message = error == null ? "unknown error" : String.valueOf(error.getMessage());
        results.put(info.getTestName(), new Result(info.getTestName(), false, TestHarness.currentTicks(), message));
        LOGGER.error("Game test {} failed: {}", info.getTestName(), message);
        TestHarness.finishCurrent();
    }

    @Override
    public void onTestSuccess(GameTestInfo info) {
        results.put(info.getTestName(), new Result(info.getTestName(), true, TestHarness.currentTicks(), null));
        TestHarness.finishCurrent();
    }

    @Override
    public void finish() {
        TestHarness.finishCurrent();
        List<String> lines = report();
        for (String line : lines) {
            LOGGER.info(line);
        }
        Path file = Paths.get(System.getProperty("user.dir"), REPORT_FILE);
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
            LOGGER.info("The same report was saved to {}", file.toAbsolutePath());
        } catch (IOException exception) {
            LOGGER.warn("Could not save the game test report: {}", exception.toString());
        }
    }

    private List<String> report() {
        List<String> lines = new ArrayList<>();
        String rule = "=".repeat(78);
        lines.add("");
        lines.add(rule);
        lines.add(" Midnight Thoughts game tests on " + TestHarness.loaderName());
        lines.add(rule);

        int passed = 0;
        int failed = 0;
        for (TestCatalog.Category category : TestCatalog.categories()) {
            List<GameTestCase> cases = TestCatalog.casesIn(category);
            cases.retainAll(TestCatalog.selected());
            if (cases.isEmpty()) continue;
            int categoryPassed = 0;
            for (GameTestCase testCase : cases) {
                Result result = results.get(testCase.id());
                if (result != null && result.passed()) categoryPassed++;
            }
            lines.add("");
            lines.add(String.format(Locale.ROOT, " %-62s %3d / %-3d passed",
                    category.title(), categoryPassed, cases.size()));
            for (GameTestCase testCase : cases) {
                Result result = results.get(testCase.id());
                String status;
                String time;
                if (result == null) {
                    status = "SKIP";
                    time = "";
                } else {
                    status = result.passed() ? "PASS" : "FAIL";
                    time = result.ticks() < 0 ? "" : String.format(Locale.ROOT, "%5d ticks", result.ticks());
                    if (result.passed()) passed++; else failed++;
                }
                lines.add(String.format(Locale.ROOT, "   %s  %-" + NAME_WIDTH + "s %s",
                        status, fit(testCase.description()), time));
                if (result != null && !result.passed()) {
                    lines.add("         reason: " + result.error());
                }
            }
        }

        int total = TestCatalog.selected().size();
        double seconds = (System.currentTimeMillis() - startedAt) / 1000.0;
        lines.add("");
        lines.add(rule);
        lines.add(String.format(Locale.ROOT, " Result: %d passed, %d failed, %d not run, %d total, %.1f s",
                passed, failed, total - passed - failed, total, seconds));
        lines.add(failed == 0 && passed == total
                ? " Everything listed below is verified on " + TestHarness.loaderName() + "."
                : " Some checks did not pass, see the reasons above.");
        lines.add(rule);
        lines.add(" What these tests cover");
        for (TestCatalog.Category category : TestCatalog.categories()) {
            List<GameTestCase> selected = TestCatalog.casesIn(category);
            selected.retainAll(TestCatalog.selected());
            if (selected.isEmpty()) continue;
            lines.add("");
            lines.add(" " + category.title());
            for (String point : category.coverage()) {
                lines.add("   * " + point);
            }
        }
        lines.add("");
        lines.add(" Not covered here, check by hand: the summary screen, themes and tooltips, HUD drawing,");
        lines.add(" the sleep overlay (moon, text, scaling, stars), hiding chat and the action bar on the client,");
        lines.add(" the config screen and Mod Menu, resource reloads, MCA, Serene Seasons, KubeJS and the live facts API.");
        lines.add(rule);
        return lines;
    }

    private static String fit(String text) {
        if (text.length() <= NAME_WIDTH) return text;
        return text.substring(0, NAME_WIDTH - 3) + "...";
    }
}
