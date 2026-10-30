package mt.gametest;

import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class MTGameTests {

    public static final String ARENA = "midnightthoughts:gametest/arena";

    public MTGameTests() {}

    @SuppressWarnings("unused")
    @GameTestGenerator
    public static Collection<TestFunction> generate() {
        List<TestFunction> functions = new ArrayList<>();
        for (GameTestCase testCase : TestCatalog.selected()) {
            functions.add(new TestFunction(
                    "midnightthoughts:" + testCase.id(),
                    testCase.id(),
                    ARENA,
                    Rotation.NONE,
                    testCase.timeoutTicks(),
                    0L,
                    true,
                    1,
                    1,
                    helper -> TestHarness.run(helper, testCase)));
        }
        return functions;
    }
}
