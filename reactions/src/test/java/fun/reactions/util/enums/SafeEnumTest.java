package fun.reactions.util.enums;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SafeEnumTest {
    @DataProvider
    public Object[][] nameData() {
        return new Object[][] {
                {Something.UWU, "UWU"},
                {Something.NYA, "NYA"},
                {null, "ANY"}
        };
    }

    @Test(dataProvider = "nameData")
    public void nameTest(Something st, String expected) {
        assertEquals(
                new SafeEnum<>(st).name(),
                expected
        );
    }

    @DataProvider
    public Object[][] isValidForData() {
        return new Object[][] {
                {new SafeEnum<>(Something.UWU), Something.UWU, true},
                {new SafeEnum<>(Something.OWO), Something.OWO, true},
                {new SafeEnum<>(Something.UWU), Something.AWOO, false},
                {new SafeEnum<>(null), Something.YAY, true},
                {new SafeEnum<>(null), Something.NYA, true}
        };
    }

    @Test(dataProvider = "isValidForData")
    public void isValidForTest(SafeEnum<Something> safeEnum, Something st, boolean expected) {
        assertEquals(
                safeEnum.isValidFor(st),
                expected
        );
    }

    enum Something {
        UWU, OWO, AWOO, YAY, NYA
    }
}