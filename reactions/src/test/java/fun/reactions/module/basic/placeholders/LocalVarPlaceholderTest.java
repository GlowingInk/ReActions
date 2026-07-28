package fun.reactions.module.basic.placeholders;

import fun.reactions.ReActions;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.Variables;
import fun.reactions.model.environment.variables.StructVariable;
import fun.reactions.placeholders.ModernPlaceholdersManager;
import fun.reactions.placeholders.PlaceholdersManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class LocalVarPlaceholderTest {
    private Environment env;

    @BeforeMethod
    public void setUp() {
        ReActions.Platform platform = mock(ReActions.Platform.class);
        PlaceholdersManager placeholdersManager = new ModernPlaceholdersManager();
        when(platform.getPlaceholders()).thenReturn(placeholdersManager);
        placeholdersManager.registerPlaceholder(new LocalVarPlaceholder());

        Variables vars = new Variables();
        vars.set("block_x", "1");

        StructVariable location = new StructVariable("here", new HashMap<>());
        location.putChild("world", Variable.simple("world"));
        StructVariable block = new StructVariable("STONE", new HashMap<>());
        block.putChild("x", Variable.simple("1"));
        block.putChild("location", location);
        vars.setVariable("block", block);

        env = new Environment(platform, "id", vars, null, 0);
    }

    @DataProvider
    public Object[][] resolveData() {
        return new Object[][] {
                {"%[local:block_x]", "1"},
                {"%[block_x]", "1"},

                {"%[local:block:x]", "1"},
                {"%[block:x]", "1"},

                {"%[local:block:location:world]", "world"},
                {"%[block:location:world]", "world"},

                {"%[block]", "STONE"},

                {"%[local:block:missing]", "STONE"},
                {"%[block:missing]", "STONE"},

                {"%[missing]", "%[missing]"},
                {"%[local:missing]", "%[local:missing]"}
        };
    }

    @Test(dataProvider = "resolveData")
    public void resolveTest(String template, String expected) {
        PlaceholdersManager manager = env.getPlatform().getPlaceholders();
        assertEquals(manager.parse(env, template), expected);
    }
}
