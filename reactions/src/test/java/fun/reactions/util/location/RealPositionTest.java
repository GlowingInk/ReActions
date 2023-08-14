package fun.reactions.util.location;

import fun.reactions.util.parameter.Parameters;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class RealPositionTest {
    @DataProvider
    public Object[][] ofData() {
        return new Object[][] {
                {"world,1,2,3,45,67", RealPosition.of("world", 1, 2, 3, 45f, 67f)},
                {"world,1,2,3", RealPosition.of("world", 1, 2, 3, 0, 0)},
        };
    }
    
    @Test(dataProvider = "ofData")
    public void ofTest(String full, RealPosition expected) {
        assertEquals(
                RealPosition.byString(full),
                expected
        );
    }

    @DataProvider
    public Object[][] fromParametersData() {
        return new Object[][] {
                {"world:world x:1 y:2 z:3 yaw:4 pitch:5", RealPosition.of("world", 1, 2, 3, 4f, 5f)},
                {"world:world x:1 y:2 z:3", RealPosition.of("world", 1, 2, 3, 0, 0)}
        };
    }

    @Test(dataProvider = "fromParametersData")
    public void fromParametersTest(String params, RealPosition expected) {
        assertEquals(
                RealPosition.fromParameters(Parameters.fromString(params)),
                expected
        );
    }
}