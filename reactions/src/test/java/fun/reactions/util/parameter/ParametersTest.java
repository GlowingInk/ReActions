package fun.reactions.util.parameter;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ParametersTest {
    @DataProvider
    public Object[][] fromStringData() {
        return new Object[][] {
                {
                        "test:{value} test2:value\\ test3:va:lue test4:value}",
                        "test:value test2:{value\\\\} test3:{va:lue} test4:{value\\}}", 4
                },
                {
                        "test:ignored test:value1 test2:\\{value2 test3:value3}",
                        "test:value1 test2:{\\{value2} test3:{value3\\}}", 3
                },
                {
                        "test:{inside:{hallo world}} test2:{inside1:{hallo world} inside2:{howa u 2day}}",
                        "test:{inside:{hallo world}} test2:{inside1:{hallo world} inside2:{howa u 2day}}", 2
                },
                {
                        "test:{{additional brackets}} empty:{} test2:\\{brackets2\\}",
                        "test:{{additional brackets}} empty:{} test2:{{brackets2}}", 3},
                {
                        "test:{value broken\\}",
                        "", 0
                },
                {
                        "test:{value{ broken}",
                        "", 0
                },
                {
                        "key:{s p a c e:fake\\}} e:test",
                        "key:{s p a c e:fake\\}} e:test", 2
                },
                {
                        "key:someverylongvaluewow!",
                        "key:{someverylongvaluewow!}", 1
                }
        };
    }

    @Test(dataProvider = "fromStringData")
    public void fromStringTest(String input, String expected, int size) {
        Parameters result = Parameters.fromString(input);
        assertTrue( // Testing Parameters#isSimilar too
                result.isSimilar(Parameters.fromString(expected))
        );
        assertEquals( // We expect the result to be the same
                Parameters.fromString(result.originFormatted()).originFormatted(),
                expected
        );
        assertEquals(result.size(), size);
    }

    @DataProvider
    public Object[][] fromMapData() {
        return new Object[][] {
                {Map.of("key", "value", "test", "other value"), "key:value test:{other value}"},
                {Map.of("test", "ends with slash\\", "empty", ""), "test:{ends with slash\\\\} empty:{}"}
        };
    }

    @Test(dataProvider = "fromMapData")
    public void fromMapTest(Map<String, String> map, String paramsStr) {
        assertTrue(Parameters.fromMap(map).isSimilar(Parameters.fromString(paramsStr)));
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
                        list:
                        - a list
                        - of
                        - values
                        """,
                        "test:value " +
                        "key:{" +
                            "child:{another value} " +
                            "num:4 " +
                            "level-further:{" +
                                "wow:{{it works}}" +
                            "}" +
                        "} " +
                        "another:{a bit longer value} " +
                        "list1:{a list} list2:of list3:values"
                }
        };
    }

    @Test(dataProvider = "fromConfigurationData")
    public void fromConfigurationTest(String cfgStr, String expected) throws InvalidConfigurationException {
        FileConfiguration cfg = new YamlConfiguration();
        cfg.loadFromString(cfgStr);
        assertTrue(
                Parameters.fromConfiguration(cfg, Set.of("ignored")).isSimilar(Parameters.fromString(expected))
        );
    }

    @DataProvider
    public static Object[][] keyedListData() {
        return new Object[][] {
                {"key1:value key2:value key3:value", List.of("key1", "key2", "key3")},
                {"key:value key1:value key2:value key3:value", List.of("key1", "key2", "key3")},
                {"key:value key2:value", List.of("key")}
        };
    }

    @Test(dataProvider = "keyedListData")
    public void keyedListTest(String input, List<String> expected) {
        assertEquals(Parameters.fromString(input).keyedList("key"), expected);
    }

    @DataProvider
    public static Object[][] escapeValueData() {
        return new Object[][] {
                {"basic text", "basic text"},
                {"", ""},
                {"\\", "\\\\"},
                {"}", "\\}"},
                {"already\\{escaped", "already\\{escaped"},
                {"on\\ly \\the last\\", "on\\ly \\the last\\\\"},
                {"test:{equal amount}", "test:{equal amount}"},
                {"test:{unequal amount}}", "test:\\{unequal amount\\}\\}"},
                {"{{unequal amount}", "\\{\\{unequal amount\\}"},
                {"{unequal escaped\\}}", "{unequal escaped\\}}"},
                {"{unequal with last}}\\", "\\{unequal with last\\}\\}\\\\"},
                {"}wrong order{", "\\}wrong order\\{"}
        };
    }

    @Test(dataProvider = "escapeValueData")
    public void escapeValueTest(String input, String expected) {
        String result = Parameters.escapeValue(input);
        assertEquals(result, expected);
        assertEquals(Parameters.escapeValue(result), expected); // Escaping the escaped should not work
    }

    @DataProvider
    public static Object[][] escapeData() {
        return new Object[][] {
                {"basic text", "basic text"},
                {"", ""},
                {"\\", "\\\\"},
                {"}", "\\}"},
                {"already\\{escaped", "already\\\\\\{escaped"},
                {"on\\ly \\the last\\", "on\\\\ly \\\\the last\\\\"},
                {"test:{equal amount}", "test\\:\\{equal amount\\}"},
                {"test:{unequal amount}}", "test\\:\\{unequal amount\\}\\}"},
                {"{{unequal amount}", "\\{\\{unequal amount\\}"},
                {"{unequal escaped\\}}", "\\{unequal escaped\\\\\\}\\}"},
                {"{unequal with last}}\\", "\\{unequal with last\\}\\}\\\\"},
                {"}wrong order{", "\\}wrong order\\{"}
        };
    }

    @Test(dataProvider = "escapeData")
    public void escapeTest(String input, String expected) {
        String result = Parameters.escape(input);
        assertEquals(result, expected);
        assertTrue(Parameters.fromString(result).isEmpty(), Parameters.fromString(result).keys().toString());
    }

    @DataProvider
    public Object[][] findKeyData() {
        return new Object[][] {
                {"my test", Parameters.ORIGIN, Parameters.ORIGIN, List.of("test", "my", "yay")},
                {"exec:activator", "exec", Parameters.ORIGIN, List.of("other", "activator", "exec")}
        };
    }

    @Test(dataProvider = "findKeyData")
    public void findKeySafeTest(String paramsStr, String expectedKey, String def, List<String> keys) {
        assertEquals(
                Parameters.fromString(paramsStr).findKey(def, keys),
                expectedKey
        );
    }

    @DataProvider
    public Object[][] getStringData() {
        return new Object[][] {
                {Parameters.fromString("key:abc anOther_value:123 EXTRA:{456} key:overridden"), "key", "overridden"},
                {Parameters.fromString("key:abc anOther_value:123 EXTRA:{456} key:overridden"), "extra", "456"},
                {Parameters.fromString("nothing!"), "key", ""},
                {Parameters.fromString("key:{Escaped \\{ \\} \\\\\\} \\: \\\\ \\ value}"), "key", "Escaped { } \\} : \\ \\ value"}
        };
    }

    @Test(dataProvider = "getStringData")
    public void getStringTest(Parameters params, String key, String expected) {
        assertEquals(
                params.getString(key),
                expected
        );
    }
}