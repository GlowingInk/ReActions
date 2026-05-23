package fun.reactions.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.placeholders.LocalVarPlaceholder;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class LegacyPlaceholdersManagerTest {
    @Test
    public void testParsePlaceholders() {
        var mgr = new LegacyPlaceholdersManager();
        mgr.registerPlaceholder(new LocalVarPlaceholder());
        PlaceholdersManager.setCountLimit(16);
        Variables vars = new Variables();
        vars.set("test", "y\\ay");
        vars.set("another", "%test%\\,");
        assertEquals(
                mgr.parse(new Environment(null, "", vars, null, 0), "Foo %another% bar %ignored%"),
                "Foo y\\ay\\, bar %ignored%"
        );
    }
}