package fun.reactions.model.environment;

import fun.reactions.model.environment.variables.StructVariable;
import org.bukkit.configuration.file.YamlConfiguration;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

public class VariablesTest {
    @Test
    public void setAndGetTest() {
        Variables vars = new Variables();
        vars.set("key", "value");
        assertEquals(vars.getString("key"), "value");
        assertEquals(vars.getString("missing"), "");
    }

    @Test
    public void changedTracksExplicitSetsOnlyTest() {
        Map<String, Variable> seed = new HashMap<>();
        seed.put("cancel_event", Variable.property(false));
        Variables vars = new Variables(seed);

        assertTrue(vars.changedBoolean("cancel_event").isEmpty());
        vars.set("cancel_event", "true");
        assertTrue(vars.changedBoolean("cancel_event").orElse(false));
    }

    @Test
    public void setVariableStoresRawVariableTest() {
        Variables vars = new Variables();
        StructVariable block = new StructVariable("STONE", new HashMap<>());
        block.setChild("x", "1");
        vars.setVariable("block", block);

        assertEquals(vars.getVariable("block"), block);
        assertEquals(vars.getString("block"), "STONE");
    }

    @Test
    public void readConfigurationKeepsFlatVariablesAsSimpleTest() throws Exception {
        var cfg = new YamlConfiguration();
        cfg.loadFromString("key: value\nother: 5\n");

        Variables vars = Variables.readConfiguration(cfg);
        assertEquals(vars.getString("key"), "value");
        assertEquals(vars.getString("other"), "5");
    }

    @Test
    public void writeThenReadConfigurationRoundTripsStructuredVariablesTest() {
        Variables vars = new Variables();
        vars.set("plain", "value");
        StructVariable block = new StructVariable("STONE", new HashMap<>());
        block.setChild("x", "1");
        block.putChild("location", StructVariable.fromRaw("here world:world x:1"));
        vars.setVariable("block", block);

        var cfg = new YamlConfiguration();
        vars.writeConfiguration(cfg);

        // Plain variables stay flat scalars (old-format-compatible), structured ones become nested sections
        assertEquals(cfg.getString("plain"), "value");
        assertTrue(cfg.isConfigurationSection("block"));
        assertFalse(cfg.isConfigurationSection("plain"));

        Variables reloaded = Variables.readConfiguration(cfg);
        assertEquals(reloaded.getString("plain"), "value");
        assertEquals(reloaded.getString("block"), "STONE");
        assertEquals(reloaded.getVariable("block").resolve("x"), "1");
        assertEquals(reloaded.getVariable("block").resolve("location:world"), "world");
    }
}
