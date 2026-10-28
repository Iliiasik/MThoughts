package mt.gametest;

import java.util.function.Consumer;

public record GameTestCase(String category, String name, String description, int timeoutTicks,
                           Consumer<TestContext> body) {

    public String id() {
        return category + "." + name;
    }
}
