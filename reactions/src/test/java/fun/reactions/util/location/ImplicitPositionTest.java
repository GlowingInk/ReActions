package fun.reactions.util.location;

import fun.reactions.util.parameter.Parameters;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class ImplicitPositionTest {
    @DataProvider
    public Object[][] ofData() {
        return new Object[][] {
                {"*,*,*,*", ImplicitPosition.EVERYWHERE},
                {"world,1,2,3,45,67", ImplicitPosition.of("world", 1, 2, 3)},
                {"*,1,2,3", ImplicitPosition.of(null, 1, 2, 3)},
                {"world,*,*,3", ImplicitPosition.of("world", null, null, 3)}
        };
    }

    @Test(dataProvider = "ofData")
    public void ofTest(String full, ImplicitPosition expected) {
        assertEquals(
                ImplicitPosition.byString(full),
                expected
        );
    }

    @DataProvider
    public Object[][] fromParametersData() {
        return new Object[][] {
                {"", ImplicitPosition.EVERYWHERE},
                {"world:world", ImplicitPosition.of("world", null, null, null)},
                {"world:world x:1 y:2 z:3", ImplicitPosition.of("world", 1, 2, 3)},
                {"world:world x:1 y:2 z:3 yaw:4 pitch:5", ImplicitPosition.of("world", 1, 2, 3)}
        };
    }

    @Test(dataProvider = "fromParametersData")
    public void fromParametersTest(String params, ImplicitPosition expected) {
        assertEquals(
                ImplicitPosition.fromParameters(Parameters.fromString(params)),
                expected
        );
    }

    @DataProvider
    public Object[][] isValidAtData() {
        return new Object[][] {
                {true, ImplicitPosition.byString("world,1,2,3"), "world", 1, 2, 3},
                {true, ImplicitPosition.byString("*,1,2,3"), "another", 1, 2, 3},
                {true, ImplicitPosition.EVERYWHERE, "world", 1, 2, 3},
                {true, ImplicitPosition.byString("world,1,*,*"), "world", 1, 3, 2},
                {false, ImplicitPosition.byString("world,*,*,*"), "another", 1, 2, 3},
                {false, ImplicitPosition.byString("world,1,*,*"), "world", 2, 1, 3},
                {false, ImplicitPosition.byString("*,1,2,3"), "another", 3, 1, 2}
        };
    }

    @Test(dataProvider = "isValidAtData")
    public void isValidAtTest(boolean expected, ImplicitPosition loc, String world, int x, int y, int z) {
        assertEquals(
                loc.isValidAt(world, x, y, z),
                expected
        );
    }
}
