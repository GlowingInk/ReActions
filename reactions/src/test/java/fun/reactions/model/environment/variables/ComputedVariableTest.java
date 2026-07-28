package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;

import static org.testng.Assert.*;

public class ComputedVariableTest {
    private static class TestVariable extends ComputedVariable {
        final int[] computeCount = new int[]{0};

        @Override
        public @NotNull String get() {
            return "self";
        }

        @Override
        protected @NotNull List<String> childKeys() {
            return List.of("a", "b");
        }

        @Override
        protected @Nullable Variable computeChild(@NotNull String key) {
            computeCount[0]++;
            return switch (key) {
                case "a" -> Variable.value("1");
                case "b" -> Variable.value("2");
                default -> null;
            };
        }

        @Override
        protected @NotNull ComputedVariable copy() {
            return new TestVariable();
        }
    }

    @Test
    public void childrenAreCachedAndPopulateEveryDeclaredKeyTest() {
        TestVariable var = new TestVariable();

        assertEquals(var.resolve("a"), "1");
        assertEquals(var.resolve("a"), "1"); // cached, not recomputed
        assertEquals(var.children().keySet(), Set.of("a", "b")); // forces "b" to be resolved too
        assertEquals(var.computeCount[0], 2); // "a" and "b", each only once

        assertEquals(var.resolve("missing"), "self"); // unknown key falls back to get()
    }

    @Test
    public void changeTrackingTest() {
        TestVariable var = new TestVariable();
        assertTrue(var.changed().isEmpty());

        assertTrue(var.setChild("a", "99"));
        assertEquals(var.resolve("a"), "99");
        assertEquals(var.changed().orElse(null), "self");

        Variable replaced = var.set("override");
        assertEquals(replaced.get(), "override");
        assertEquals(replaced.changed().orElse(null), "override");
    }

    @Test
    public void forkIsolatesOverriddenChildrenTest() {
        TestVariable var = new TestVariable();
        var.putChild("a", Variable.value("overridden"));

        Variable forked = var.fork();
        assertNotSame(forked, var);
        assertEquals(forked.resolve("a"), "overridden");

        forked.setChild("a", "fork-only");
        assertEquals(forked.resolve("a"), "fork-only");
        assertEquals(var.resolve("a"), "overridden"); // original untouched by mutating the fork
    }
}
