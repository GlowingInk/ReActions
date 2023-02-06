package me.fromgate.reactions.placeholders;

import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.logic.context.Variables;
import me.fromgate.reactions.module.basics.placeholders.PlaceholderLocalVariable;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class LegacyPlaceholdersManagerTest {
    @Test
    public void testParsePlaceholders() {
        PlaceholdersManager mgr = new LegacyPlaceholdersManager();
        mgr.registerPlaceholder(new PlaceholderLocalVariable());
        PlaceholdersManager.setCountLimit(16);
        Variables vars = new Variables();
        vars.set("test", "yay");
        vars.set("another", "%test%");
        assertEquals(
                mgr.parsePlaceholders(new Environment("", vars, null), "Foo %another% bar %ignored%"),
                "Foo yay bar %ignored%"
        );
    }
}