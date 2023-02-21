package fun.reactions.placeholders;

import fun.reactions.logic.environment.Environment;
import fun.reactions.logic.environment.Variables;
import fun.reactions.module.basics.placeholders.LocalVarPlaceholder;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class LegacyPlaceholdersManagerTest {
    @Test
    public void testParsePlaceholders() {
        var mgr = new LegacyPlaceholdersManager();
        mgr.registerPlaceholder(new LocalVarPlaceholder());
        PlaceholdersManager.setCountLimit(16);
        Variables vars = new Variables();
        vars.set("test", "yay");
        vars.set("another", "%test%,");
        assertEquals(
                mgr.parse(new Environment(null, "", vars, null), "Foo %another% bar %ignored%"),
                "Foo yay, bar %ignored%"
        );
    }
}