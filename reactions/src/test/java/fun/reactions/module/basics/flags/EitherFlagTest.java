package fun.reactions.module.basics.flags;

import fun.reactions.ReActions;
import fun.reactions.model.activity.ActivitiesRegistry;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variables;
import fun.reactions.test.MockFlag;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class EitherFlagTest {
    @Test
    public void testProceed() {
        ActivitiesRegistry registry = new ActivitiesRegistry();
        ReActions.Platform platform = Mockito.mock(ReActions.Platform.class);
        Mockito.when(platform.getActivities()).thenReturn(registry);
        registry.registerFlag(new MockFlag(false, false, "test1"));
        registry.registerFlag(new MockFlag(false, true, "test2"));
        EitherFlag flag = new EitherFlag();
        assertTrue(flag.proceed(
                new Environment(platform, "", new Variables(), null),
                "test1:{some value} test2:other test1:repeat"
        ));
        assertFalse(flag.proceed(
                new Environment(platform, "", new Variables(), null),
                "test1:{some value} test1:repeat"
        ));
    }
}