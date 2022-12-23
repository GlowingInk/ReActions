package me.fromgate.reactions.util.parameter;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static me.fromgate.reactions.util.parameter.Parameters.*;
import static org.testng.Assert.assertEquals;

public class ParametersTest {
    @DataProvider
    public Object[][] fromStringData() {
        return new Object[][] {
                {"test:{value} test2:value\\ test3:va:lue test4:value}", "test:value test2:{value\\\\} test3:va:lue test4:{value\\}}"},
                {"test:ignored test:value1 test2:\\{value2 test3:value3}", "test:value1 test2:{\\{value2} test3:{value3\\}}"},
                {"test:{{additional brackets}} empty:{} test2:\\{brackets2\\}", "test:{{additional brackets}} empty:{} test2:{{brackets2}}"},
                {"test:{value broken\\}", ""},
                {"test:{value{ broken}", ""},
                {"key:{s p a c e:fake\\}} e:test", "key:{s p a c e:fake\\}} e:test"},
                {"key:someverylongvaluewow!", "key:{someverylongvaluewow!}"}
        };
    }

    @Test(dataProvider = "fromStringData")
    public void testFromString(String input, String expected) {
        String result = fromString(input).toString();
        assertEquals(result, expected);
        assertEquals(fromString(result).originFormatted(), expected); // We expect the result to be the same
    }

    @DataProvider
    public Object[][] fromMapData() {
        return new Object[][] {
                {Map.of("key", "value", "test", "other value"), "key:value test:{other value}"},
                {Map.of("test", "ends with slash\\", "empty", ""), "test:{ends with slash\\\\} empty:{}"}
        };
    }

    @Test(dataProvider = "fromMapData")
    public void testFromMapData(Map<String, String> map, String paramsStr) {
        assertEquals(fromMap(map), fromString(paramsStr));
    }

    @DataProvider
    public Object[][] fromConfigurationData() {
        return new Object[][] {
                {       """
                        test: value
                        key:
                            child: another value
                            num: 4
                            level-further:
                                wow: '{it works}'
                            ignored: totally
                        another: a bit longer value
                        """,
                        "test:value key:{child:{another value} num:4 level-further:{wow:{{it works}}}} another:{a bit longer value}"
                }
        };
    }

    @Test(dataProvider = "fromConfigurationData")
    public void testFromConfiguration(String cfgStr, String expected) throws InvalidConfigurationException {
        FileConfiguration cfg = new YamlConfiguration();
        cfg.loadFromString(cfgStr);
        assertEquals(
                fromConfiguration(cfg, Set.of("ignored")),
                fromString(expected)
        );
    }

    @DataProvider
    public static Object[][] escapeParametersData() {
        return new Object[][] {
                {"basic text", "basic text"},
                {"", ""},
                {"\\", "\\\\"},
                {"}", "\\}"},
                {"already\\{escaped", "already\\{escaped"},
                {"on\\ly \\the last\\", "on\\ly \\the last\\\\"},
                {"{equal amount}", "{equal amount}"},
                {"{unequal amount}}", "\\{unequal amount\\}\\}"},
                {"{{unequal amount}", "\\{\\{unequal amount\\}"},
                {"{unequal escaped\\}}", "{unequal escaped\\}}"},
                {"{unequal with last}}\\", "\\{unequal with last\\}\\}\\\\"},
                {"}wrong order{", "\\}wrong order\\{"}
        };
    }

    @Test(dataProvider = "escapeParametersData")
    public void testEscapeParameters(String input, String expected) {
        String result = escapeParameters(input);
        assertEquals(result, expected);
        assertEquals(escapeParameters(result), expected); // Escaping the escaped should not work
    }

    @DataProvider
    public static Object[][] getKeyListData() {
        return new Object[][] {
                {"key1:value key2:value key3:value", List.of("key1", "key2", "key3")},
                {"key:value key1:value key2:value key3:value", List.of("key1", "key2", "key3")},
                {"key:value key2:value", List.of("key")}
        };
    }

    @Test(dataProvider = "getKeyListData")
    public void testGetKeyList(String input, List<String> expected) {
        assertEquals(fromString(input).getKeyList("key"), expected);
    }
}