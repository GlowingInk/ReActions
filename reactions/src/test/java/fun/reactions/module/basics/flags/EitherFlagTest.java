package fun.reactions.module.basics.flags;

import fun.reactions.logic.activity.ActivitiesRegistry;
import fun.reactions.logic.environment.Environment;
import fun.reactions.logic.environment.Variables;
import fun.reactions.test.MockFlag;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class EitherFlagTest {
    @Test
    public void testProceed() {
        ActivitiesRegistry registry = new ActivitiesRegistry();
        registry.registerFlag(new MockFlag(false, false, "test1"));
        registry.registerFlag(new MockFlag(false, true, "test2"));
        EitherFlag flag = new EitherFlag(registry);
        assertTrue(flag.proceed(
                new Environment(null, "", new Variables(), null),
                "test1:{some value} test2:other test1:repeat"
        ));
        assertFalse(flag.proceed(
                new Environment(null, "", new Variables(), null),
                "test1:{some value} test1:repeat"
        ));
    }
}