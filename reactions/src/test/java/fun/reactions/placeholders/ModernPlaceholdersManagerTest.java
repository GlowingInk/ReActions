package fun.reactions.placeholders;

import fun.reactions.ReActions;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.placeholders.LocalVarPlaceholder;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class ModernPlaceholdersManagerTest {
    @Test
    public void testParsePlaceholders() {
        var platform = Mockito.mock(ReActions.Platform.class);
        var mgr = new ModernPlaceholdersManager();
        when(platform.getPlaceholders()).thenReturn(mgr);
        mgr.registerPlaceholder(new LocalVarPlaceholder());
        PlaceholdersManager.setCountLimit(16);
        Variables vars = new Variables();
        vars.set("test", "y\\ay");
        vars.set("another", "%[test]\\,");
        assertEquals(
                mgr.parse(new Environment(platform, "", vars, null, 0), "Foo %[another] bar %[ignored]"),
                "Foo y\\ay\\, bar %[ignored]"
        );
    }
}