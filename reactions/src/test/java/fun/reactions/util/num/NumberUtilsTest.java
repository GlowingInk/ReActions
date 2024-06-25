package fun.reactions.util.num;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.OptionalDouble;
import java.util.function.DoublePredicate;

import static org.testng.Assert.assertEquals;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class NumberUtilsTest {
    @DataProvider
    public static Object[][] parseDoubleData() {
        return new Object[][]{
                {"3.14", Is.POSITIVE, OptionalDouble.of(3.14)},
                {"abc", Is.NEGATIVE, OptionalDouble.empty()}
        };
    }

    @Test(dataProvider = "parseDoubleData")
    public void parseDoubleTest(String str, DoublePredicate predicate, OptionalDouble expected) {
        assertEquals(
                NumberUtils.parseDouble(str, predicate),
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