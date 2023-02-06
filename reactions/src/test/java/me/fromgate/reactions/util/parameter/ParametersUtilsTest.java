package me.fromgate.reactions.util.parameter;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static me.fromgate.reactions.util.parameter.ParametersUtils.escapeParameters;
import static me.fromgate.reactions.util.parameter.ParametersUtils.splitSafely;
import static org.testng.Assert.assertEquals;

public class ParametersUtilsTest {

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
    public void escapeParametersTest(String input, String expected) {
        String result = escapeParameters(input);
        assertEquals(result, expected);
        assertEquals(escapeParameters(result), expected); // Escaping the escaped should not work
    }

    @DataProvider
    public static Object[][] splitSafelyData() {
        return new Object[][] {
                {
                    "var_compare:{id:MiniE1 value:%CEfasty%} var_compare:{id:MiniE2 value:%CEfasty%}", ' ',
                    "var_compare:{id:MiniE1 value:%CEfasty%}", "var_compare:{id:MiniE2 value:%CEfasty%}"
                },
                {
                    "key:value other:value; another:{value; value}", ';',
                    "key:value other:value", " another:{value; value}"
                },
                {
                    "key:value;value ; another:{value; value}", ';',
                    "key:value;value ", " another:{value; value}"
                },
                {
                    "key:{my_value}; another:{value ;\\} value}", ';',
                    "key:{my_value}", " another:{value ;\\} value}"
                },
                {
                    "key:my_value; multiple:times; with zero:length;", ';',
                    "key:my_value", " multiple:times", " with zero:length", ""
                }
        };
    }

    @Test(dataProvider = "splitSafelyData")
    public void splitSafelyTest(String str, char splitCh, String... expected) {
        assertEquals(splitSafely(str, splitCh), List.of(expected));
    }
}
