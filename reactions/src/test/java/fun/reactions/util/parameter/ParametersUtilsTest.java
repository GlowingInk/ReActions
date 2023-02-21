package fun.reactions.util.parameter;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

public class ParametersUtilsTest {
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
        Assert.assertEquals(ParametersUtils.splitSafely(str, splitCh), List.of(expected));
    }
}
