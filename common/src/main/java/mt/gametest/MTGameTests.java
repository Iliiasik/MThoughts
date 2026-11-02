package mt.gametest;

import mt.MTConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;

import java.util.List;
import java.util.function.Consumer;

public final class MTGameTests {

    public static final Identifier ARENA = Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, "gametest/arena");

    private MTGameTests() {}

    public record Entry(Identifier id, Consumer<GameTestHelper> function, int maxTicks) {

        public ResourceKey<Consumer<GameTestHelper>> functionKey() {
            return ResourceKey.create(Registries.TEST_FUNCTION, id);
        }

        public Identifier environmentId() {
            return id.withPrefix("environment/");
        }

        public GameTestInstance instance(Holder<TestEnvironmentDefinition<?>> environment) {
            return new FunctionGameTestInstance(functionKey(),
                    new TestData<>(environment, ARENA, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0));
        }
    }

    public static List<Entry> entries() {
        return TestCatalog.selected().stream()
                .map(testCase -> new Entry(Identifier.fromNamespaceAndPath(MTConstants.MOD_ID, testCase.id()),
                        helper -> TestHarness.run(helper, testCase), testCase.timeoutTicks()))
                .toList();
    }
}
