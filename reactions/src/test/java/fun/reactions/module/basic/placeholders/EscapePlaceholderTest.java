package fun.reactions.module.basic.placeholders;

import fun.reactions.ReActions;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variables;
import fun.reactions.placeholders.ModernPlaceholdersManager;
import fun.reactions.placeholders.PlaceholdersManager;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class EscapePlaceholderTest {
    @DataProvider
    public static Object[][] resolveData() {
        return new Object[][]{
                {"esc-ph", "%[esc-ph]", "placeholders|esc-ph", "\\%[esc-ph\\]"},
                {"esc-prm", "\\ {test}\\", "params|esc-prm", "\\ {test}\\\\"},
                {"esc-prm-f", "\\ {test}\\", "params-full|esc-prm-f", "\\\\ \\{test\\}\\\\"},
                {"esc-inky", "&aHello", "inky|esc-inky", "\\&aHello"},
                {"esc-default", "some %[test]}", "esc-default", "some \\%[test\\]\\\\}"}
        };
    }


    @Test(dataProvider = "resolveData")
    public void resolveTest(String varKey, String varValue, String arg, String expected) {
        PlaceholdersManager placeholdersManager = new ModernPlaceholdersManager();
        ReActions.Platform platform = mock(ReActions.Platform.class);
        when(platform.getPlaceholders()).thenReturn(placeholdersManager);

        EscapePlaceholder escapePlaceholder = new EscapePlaceholder();
        LocalVarPlaceholder varPlaceholder = new LocalVarPlaceholder();
        placeholdersManager.registerPlaceholder(escapePlaceholder);
        placeholdersManager.registerPlaceholder(varPlaceholder);

        Variables vars = new Variables();

        Environment env = new Environment(platform, "id", vars, null, 0, false);

        vars.set(varKey, varValue);
        assertEquals(
                escapePlaceholder.resolve(env, "escape", arg),
                expected
        );
    }
}