package fun.reactions.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.placeholders.LocalVarPlaceholder;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ModernPlaceholdersManagerTest {
    @Test
    public void testParsePlaceholders() {
        var mgr = new ModernPlaceholdersManager();
        mgr.registerPlaceholder(new LocalVarPlaceholder());
        PlaceholdersManager.setCountLimit(16);
        Variables vars = new Variables();
        vars.set("test", "yay");
        vars.set("another", "%[test],");
        assertEquals(
                mgr.parse(new Environment(null, "", vars, null), "Foo %[another] bar %[ignored]"),
                "Foo yay, bar %[ignored]"
        );
    }
}