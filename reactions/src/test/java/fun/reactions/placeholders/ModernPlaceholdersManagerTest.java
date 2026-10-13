package fun.reactions.placeholders;

import fun.reactions.ReActions;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.placeholders.LocalVarPlaceholder;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ModernPlaceholdersManagerTest {
    @DataProvider
    public Object[][] parseData() {
        return new Object[][] {
                {"Foo %[another] bar %[ignored]", "Foo y\\ay\\, bar %[ignored]"},
                {"Foo %[a%b] bar", "Foo plain bar"},
                {"Foo %[a%%[x]] bar", "Foo plain bar"},
                {"Foo %[a%b%] bar", "Foo trailing bar"},
                {"100% %[x]", "100% b"}
        };
    }

    @Test(dataProvider = "parseData")
    public void testParsePlaceholders(String text, String expected) {
        var platform = Mockito.mock(ReActions.Platform.class);
        var mgr = new ModernPlaceholdersManager();
        when(platform.getPlaceholders()).thenReturn(mgr);
        mgr.registerPlaceholder(new LocalVarPlaceholder());
        mgr.countLimit = 16;
        Variables vars = new Variables();
        vars.set("test", "y\\ay");
        vars.set("another", "%[test]\\,");
        vars.set("a%b", "plain");
        vars.set("x", "b");
        vars.set("a%b%", "trailing");
        assertEquals(mgr.parse(new Environment(platform, "", vars, null, 0), text), expected);
    }
}
