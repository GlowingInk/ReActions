package fun.reactions.menu;

import fun.reactions.util.parameter.Parameters;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class MenuTest {
    @DataProvider
    public Object[][] fromParametersData() {
        return new Object[][] {
                {
                        "",
                        Menu.DEFAULT_TITLE, Key.key("generic_9x1"), 9, true, List.of()
                },
                {
                        "size:10 title:{Test} slot1:DIRT exec1:first exec3:third close:false",
                        "Test", Key.key("generic_9x2"), 18, false, List.of(new Menu.Slot("DIRT", "first"), Menu.Slot.EMPTY, new Menu.Slot("", "third"))
                },
                {
                        "type:HOPPER size:27 slot1:DIRT exec3:third",
                        Menu.DEFAULT_TITLE, Key.key("hopper"), 3, true, List.of(new Menu.Slot("DIRT", ""), Menu.Slot.EMPTY, new Menu.Slot("", "third"))
                },
                {
                        "type:custom:menu",
                        Menu.DEFAULT_TITLE, Key.key("custom", "menu"), 0, true, List.of()
                },
                {
                        "type:generic_9x6 size:9",
                        Menu.DEFAULT_TITLE, Key.key("generic_9x6"), 54, true, List.of()
                },
                {
                        "size:100",
                        Menu.DEFAULT_TITLE, Key.key("generic_9x6"), 54, true, List.of()
                }
        };
    }

    @Test(dataProvider = "fromParametersData")
    public void fromParametersTest(
            String params,
            String title,
            Key type,
            int size,
            boolean close,
            List<Menu.Slot> slots
    ) {
        assertMenu(Menu.fromParameters(Parameters.fromString(params)), title, type, size, close, slots);
    }

    @Test(expectedExceptions = InvalidKeyException.class)
    public void fromParametersInvalidTypeTest() {
        Menu.fromParameters(Parameters.fromString("type:{not a key}"));
    }

    @DataProvider
    public Object[][] fromConfigData() {
        return new Object[][] {
                {
                        "",
                        Menu.DEFAULT_TITLE, Key.key("generic_9x1"), 9, true, List.of()
                },
                {
                        """
                        title: Test
                        size: 18
                        close: false
                        slot1:
                          item: DIRT
                          activator: first
                        slot3:
                          activator: third
                        """,
                        "Test", Key.key("generic_9x2"), 18, false, List.of(new Menu.Slot("DIRT", "first"), Menu.Slot.EMPTY, new Menu.Slot("", "third"))
                },
                {
                        """
                        type: anvil
                        size: 54
                        slot3:
                          item: DIRT
                          activator: result
                        """,
                        Menu.DEFAULT_TITLE, Key.key("anvil"), 3, true, List.of(Menu.Slot.EMPTY, Menu.Slot.EMPTY, new Menu.Slot("DIRT", "result"))
                }
        };
    }

    @Test(dataProvider = "fromConfigData")
    public void fromConfigTest(
            String yaml,
            String title,
            Key type,
            int size,
            boolean close,
            List<Menu.Slot> slots
    ) throws InvalidConfigurationException {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.loadFromString(yaml);
        assertMenu(Menu.fromConfig(cfg), title, type, size, close, slots);
    }

    @DataProvider
    public Object[][] saveData() {
        return new Object[][] {
                {
                        "title:{Test}",
                        """
                        title: Test
                        type: generic_9x1
                        """
                },
                {
                        "size:18 title:{Test} slot2:DIRT exec2:act exec18:last close:false",
                        """
                        title: Test
                        type: generic_9x2
                        close: false
                        slot2:
                          item: DIRT
                          activator: act
                        slot18:
                          activator: last
                        """
                },
                {
                        "type:hopper slot2:DIRT",
                        """
                        title: '&4Re&6Actions Menu'
                        type: hopper
                        slot2:
                          item: DIRT
                        """
                },
                {
                        "type:custom:menu exec1:act",
                        """
                        title: '&4Re&6Actions Menu'
                        type: custom:menu
                        slot1:
                          activator: act
                        """
                }
        };
    }

    @Test(dataProvider = "saveData")
    public void saveTest(String params, String expected) {
        YamlConfiguration cfg = new YamlConfiguration();
        Menu.fromParameters(Parameters.fromString(params)).save(cfg);
        assertEquals(
                cfg.saveToString(),
                expected
        );
    }

    private static void assertMenu(Menu menu, String title, Key type, int size, boolean close, List<Menu.Slot> slots) {
        assertEquals(menu.title(), title);
        assertEquals(menu.type(), type);
        assertEquals(menu.size(), size);
        assertEquals(menu.closeOnClick(), close);
        for (int i = 0; i < size; i++) {
            assertEquals(menu.slot(i), i < slots.size() ? slots.get(i) : Menu.Slot.EMPTY);
        }
    }
}
