package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import fun.reactions.util.parameter.Parameters;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class StructVariableTest {
    @DataProvider
    public Object[][] fromRawData() {
        return new Object[][] {
                {"STONE", "STONE", "STONE"},
                {"x:1 y:2", "", "1"},
                {"here x:1 y:2 z:{deep w:1}", "here", "1"}
        };
    }

    @Test(dataProvider = "fromRawData")
    public void fromRawTest(String raw, String expectedSelf, String expectedX) {
        Variable var = StructVariable.fromRaw(raw);
        assertEquals(var.get(), expectedSelf);
        assertEquals(var.resolve("x"), expectedX);
    }

    @Test
    public void fromRawNestsArbitrarilyDeepAndLeavesCollapseTest() {
        Variable var = StructVariable.fromRaw("here x:1 y:2 z:{deep w:1}");
        assertEquals(var.resolve("z"), "deep");
        assertEquals(var.resolve("z:w"), "1");
        assertEquals(var.resolve("z:missing"), "deep"); // no such child, falls back to the node's own value

        Variable leaf = StructVariable.fromRaw("STONE");
        assertTrue(leaf.children().isEmpty()); // no keys at all - collapses to a plain leaf
        assertEquals(leaf.get(), "STONE");
    }

    @Test
    public void fromParametersTest() {
        Parameters params = Parameters.fromString(
                "value:STONE children:{x:1 y:2 location:{x:1 y:2 z:3 world:world}}"
        );
        Variable var = StructVariable.fromParameters(params);
        assertEquals(var.get(), "STONE");
        assertEquals(var.resolve("x"), "1");
        assertEquals(var.resolve("location:world"), "world");

        Variable withoutChildren = StructVariable.fromParameters(Parameters.fromString("value:STONE"));
        assertTrue(withoutChildren.children().isEmpty()); // no "children" block - collapses to a plain leaf
        assertEquals(withoutChildren.get(), "STONE");
    }

    @Test
    public void ofFlatTreatsEveryKeyAsALiteralLeafTest() {
        Parameters params = Parameters.fromString("name:Foo enchant:{sharpness:5}");
        Variable var = StructVariable.ofFlat("DIAMOND_SWORD", params);

        assertEquals(var.get(), "DIAMOND_SWORD");
        assertEquals(var.resolve("name"), "Foo");
        assertEquals(var.resolve("enchant"), "sharpness:5"); // literal value, not reinterpreted as nested structure
        assertTrue(var.child("enchant").children().isEmpty());
    }
}
