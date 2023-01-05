package me.fromgate.reactions.util;

import me.fromgate.reactions.util.NumberUtils.Is;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Predicate;

import static org.testng.Assert.assertEquals;

public class NumberUtilsTest {
    @DataProvider
    public Object[][] isNumberData() {
        return new Object[][] {
                {"123", List.of(), true},
                {"123.1", List.of(Is.INTEGER), false},
                {"-123.4", List.of(Is.POSITIVE.negate()), true},
                {"-123", List.of(Is.INTEGER, Is.NON_ZERO), true}
        };
    }

    @Test(dataProvider = "isNumberData")
    public void isNumberTest(String input, List<Predicate<String>> flags, boolean expected) {
        assertEquals(
                NumberUtils.isNumber(input, flags),
                expected
        );
    }

    @DataProvider
    public Object[][] trimDoubleData() {
        return new Object[][] {
                {0.123456, 0.1234},
                {100000.77777777, 100000.7777},
                {0.1, 0.1},
                {265.33336, 265.3333},
                {0.42344534534553453453, 0.4234},
                {100.00001, 100}
        };
    }

    @Test(dataProvider = "trimDoubleData")
    public void trimDoubleTest(double input, double expected) {
        assertEquals(
                NumberUtils.trimDouble(input),
                expected
        );
    }
}