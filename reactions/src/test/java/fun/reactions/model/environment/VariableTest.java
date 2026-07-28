package fun.reactions.model.environment;

import fun.reactions.model.environment.variables.StructVariable;
import fun.reactions.util.parameter.Parameters;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Optional;

import static org.testng.Assert.*;

public class VariableTest {
    @DataProvider
    public Object[][] resolveData() {
        return new Object[][] {
                {"", "here"},
                {"x", "1"},
                {"z", "deep"},
                {"z:w", "1"},
                {"missing", "here"},
                {"z:missing", "deep"},
                {"x:self", "1"}, // leaf, no children - falls back to get()
                {"z:self", "w:1"} // structured node - returns its Parameters string form
        };
    }

    @Test(dataProvider = "resolveData")
    public void resolveTest(String params, String expected) {
        assertEquals(StructVariable.fromRaw("here x:1 y:2 z:{deep w:1}").resolve(params), expected);
    }

    @Test
    public void setChildTest() {
        Variable root = StructVariable.fromRaw("here x:1 z:{deep w:1}");
        Variable z = root.child("z");

        assertTrue(root.setChild("z:w", "2")); // propagates up the chain
        assertEquals(root.resolve("z:w"), "2");
        assertTrue(root.changed().isPresent()); // intermediate node marked too, not just the leaf
        assertTrue(z.changed().isPresent());

        assertFalse(root.setChild("missing:deep", "value")); // no auto-vivification of missing paths
    }

    @Test
    public void changedTracksExplicitSetsOnlyTest() {
        for (Variable fresh : new Variable[] {Variable.value("a"), Variable.value("a")}) {
            assertTrue(fresh.changed().isEmpty());
            assertEquals(fresh.set("b").changed(), Optional.of("b"));
        }

        assertEquals(Variable.EMPTY.set("x").changed(), Optional.of("x"));

        Variable lazy = Variable.lazy(() -> "computed");
        assertTrue(lazy.changed().isEmpty());
        Variable overridden = lazy.set("override");
        assertNotSame(overridden, lazy);
        assertEquals(overridden.changed(), Optional.of("override"));
    }

    @Test
    public void selfAndAsParametersTest() {
        Variable root = StructVariable.fromRaw("here x:1 y:2 z:{deep w:1}");

        // "self" on a structured node returns the same string form as asParameters()
        for (String result : new String[] {root.resolve("self"), root.asParameters().toString()}) {
            Parameters params = Parameters.fromString(result);
            assertEquals(params.getString("x"), "1");
            assertEquals(params.getString("y"), "2");
            assertEquals(Parameters.fromString(params.getString("z")).getString("w"), "1");
        }

        assertTrue(Variable.value("STONE").asParameters().isEmpty());

        StructVariable shadowed = new StructVariable("here", new HashMap<>());
        shadowed.putChild("self", Variable.value("shadowed"));
        assertNotEquals(shadowed.resolve("self"), "shadowed"); // never returns a real child's raw value
        assertEquals(shadowed.child("self").get(), "shadowed"); // direct child access still works though
    }

    @Test
    public void forkTest() {
        StructVariable struct = new StructVariable("here", new HashMap<>());
        struct.setChild("x", "1");
        assertTrue(struct.changed().isPresent());

        Variable forked = struct.fork();
        assertTrue(forked.changed().isEmpty()); // fork resets tracking
        assertEquals(forked.resolve("x"), "1");

        forked.setChild("x", "2");
        assertEquals(forked.resolve("x"), "2");
        assertEquals(struct.resolve("x"), "1"); // original untouched by mutating the fork
    }
}
