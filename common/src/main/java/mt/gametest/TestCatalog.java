package mt.gametest;

import java.util.ArrayList;
import java.util.List;

public final class TestCatalog {

    public record Category(String id, String title, List<String> coverage) {}

    public static final String FILTER_PROPERTY = "midnightthoughts.gametest.only";

    private static List<Category> categories;
    private static List<GameTestCase> cases;

    private TestCatalog() {}

    static synchronized List<GameTestCase> all() {
        if (cases == null) build();
        return cases;
    }

    static synchronized List<Category> categories() {
        if (categories == null) build();
        return categories;
    }

    static List<GameTestCase> selected() {
        String filter = System.getProperty(FILTER_PROPERTY, "").trim();
        if (filter.isEmpty()) return all();
        List<GameTestCase> result = new ArrayList<>();
        for (GameTestCase testCase : all()) {
            for (String prefix : filter.split(",")) {
                if (!prefix.isBlank() && testCase.id().startsWith(prefix.trim())) {
                    result.add(testCase);
                    break;
                }
            }
        }
        return result;
    }

    static List<GameTestCase> casesIn(Category category) {
        List<GameTestCase> result = new ArrayList<>();
        for (GameTestCase testCase : all()) {
            if (testCase.category().equals(category.id())) result.add(testCase);
        }
        return result;
    }

    private static void build() {
        List<Category> builtCategories = new ArrayList<>();
        List<GameTestCase> builtCases = new ArrayList<>();
        ComfortGameTests.register(builtCategories, builtCases);
        WellRestedGameTests.register(builtCategories, builtCases);
        SleepGameTests.register(builtCategories, builtCases);
        SleepingHudGameTests.register(builtCategories, builtCases);
        FileGameTests.register(builtCategories, builtCases);
        ContentGameTests.register(builtCategories, builtCases);
        CommandGameTests.register(builtCategories, builtCases);
        AchievementGameTests.register(builtCategories, builtCases);
        ExtraGameTests.register(builtCategories, builtCases);
        categories = List.copyOf(builtCategories);
        cases = List.copyOf(builtCases);
    }
}
