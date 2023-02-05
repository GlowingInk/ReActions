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
                {"test:{value} test2:value\\ test3:va:lue test4:value}", "test:value test2:{value\\\\} test3:{va:lue} test4:{value\\}}", 4},
                {"test:ignored test:value1 test2:\\{value2 test3:value3}", "test:value1 test2:{\\{value2} test3:{value3\\}}", 3},
                {"test:{{additional brackets}} empty:{} test2:\\{brackets2\\}", "test:{{additional brackets}} empty:{} test2:{{brackets2}}", 3},
                {"test:{value broken\\}", "", 0},
                {"test:{value{ broken}", "", 0},
                {"key:{s p a c e:fake\\}} e:test", "key:{s p a c e:fake\\}} e:test", 2},
                {"key:someverylongvaluewow!", "key:{someverylongvaluewow!}", 1}
        };
    }

    @Test(dataProvider = "fromStringData")
    public void fromStringTest(String input, String expected, int size) {
        Parameters result = fromString(input);
        assertEquals(result, fromString(expected)); // Testing Parameters#equals too
        assertEquals(fromString(result.originFormatted()).originFormatted(), expected); // We expect the result to be the same
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
        assertEquals(
                fromConfiguration(cfg, Set.of("ignored")),
                fromString(expected)
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
        assertEquals(fromString(input).keyedList("key"), expected);
    }
}