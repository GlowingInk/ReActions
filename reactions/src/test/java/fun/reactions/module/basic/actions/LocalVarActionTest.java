package fun.reactions.module.basic.actions;

import fun.reactions.ReActions;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variables;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class LocalVarActionTest {
    @DataProvider
    public Object[][] proceedData() {
        return new Object[][] {
                // plain key:value - path "" resolves to get(), unifying it with the nested cases below
                {
                        List.of("key:foo value:bar"),
                        "foo",
                        List.of(""),
                        List.of("bar"),
                        true
                },
                // nested key creates a struct and sets its child
                {
                        List.of("key:block:x value:5"),
                        "block",
                        List.of("x"),
                        List.of("5"),
                        true
                },
                // a second nested call reuses the existing root instead of overwriting it
                {
                        List.of("key:block:x value:5", "key:block:y value:6"),
                        "block",
                        List.of("x", "y"),
                        List.of("5", "6"),
                        true
                },
                // "children:{...}" declares a whole structured variable in one go - a fresh
                // declaration, not a mutation, so it's correctly not marked "changed"
                {
                        List.of("key:block value:STONE children:{x:1 y:2 location:{x:1 y:2 z:3 world:world}}"),
                        "block",
                        List.of("", "x", "location:world"),
                        List.of("STONE", "1", "world"),
                        false
                }
        };
    }

    @Test(dataProvider = "proceedData")
    public void proceedTest(List<String> statements, String rootKey, List<String> paths, List<String> expected, boolean expectChanged) {
        Variables vars = new Variables();
        ReActions.Platform platform = Mockito.mock(ReActions.Platform.class);
        Environment env = new Environment(platform, "", vars, null, 0);
        LocalVarAction action = new LocalVarAction();

        for (String statement : statements) {
            assertTrue(action.proceed(env, statement));
        }

        for (int i = 0; i < paths.size(); i++) {
            assertEquals(vars.getVariable(rootKey).resolve(paths.get(i)), expected.get(i));
        }
        assertEquals(vars.getVariable(rootKey).changed().isPresent(), expectChanged);
    }
}
