package me.fromgate.reactions.util.parameter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static me.fromgate.reactions.util.parameter.Parameters.escapeParameters;
import static me.fromgate.reactions.util.parameter.Parameters.fromString;
import static org.testng.Assert.assertEquals;

public class ParametersTest {
    @DataProvider
    public Object[][] fromStringData() {
        return new Object[][] {
                {"test:{value} test2:value\\ test3:va:lue test4:value}", "test:value test2:{value\\\\} test3:va:lue test4:{value\\}}"},
                {"test:value1 test:value2", "test:value2"},
                {"test:{value broken\\}", ""},
                {"test:{value{ broken}", ""},
                {"key:{s p a c e:fake\\}} e:test", "key:{s p a c e:fake\\}} e:test"},
                {"key:someverylongvaluewow!", "key:{someverylongvaluewow!}"}
        };
    }

    @DataProvider
    public static Object[][] escapeParametersData() {
        return new Object[][] {
                {"basic text", "basic text"},
                {"\\", "\\\\"},
                {"}", "\\}"},
                {"already\\{escaped", "already\\{escaped"},
                {"only \\the last\\", "only \\the last\\\\"},
                {"{equal amount}", "{equal amount}"},
                {"{unequal amount}}", "\\{unequal amount\\}\\}"},
                {"{unequal escaped\\}}", "{unequal escaped\\}}"},
                {"}wrong order{", "\\}wrong order\\{"}
        };
    }

    @DataProvider
    public static Object[][] keyListOfData() {
        return new Object[][] {
                {"key1:value key2:value key3:value", List.of("key1", "key2", "key3")},
                {"key:value key1:value key2:value key3:value", List.of("key1", "key2", "key3")},
                {"key:value key2:value", List.of("key", "key2")}
        };
    }

    @Test(dataProvider = "fromStringData")
    public void testFromString(String input, String expected) {
        String result = fromString(input).toString();
        assertEquals(result, expected);
        assertEquals(fromString(result).originFormatted(), expected);
    }

    @Test(dataProvider = "escapeParametersData")
    public void testEscapeParameters(String input, String expected) {
        String result = escapeParameters(input);
        assertEquals(result, expected);
        assertEquals(escapeParameters(result), expected);
    }

    @Test(dataProvider = "keyListOfData")
    public void testKeyListOf(String input, List<String> expected) {
        assertEquals(Parameters.fromString(input).keyListOf("key"), expected);
    }
}