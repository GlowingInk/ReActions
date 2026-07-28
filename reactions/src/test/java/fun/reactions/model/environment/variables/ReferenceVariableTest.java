package fun.reactions.model.environment.variables;

import fun.reactions.model.environment.Variable;
import org.testng.annotations.Test;

import java.util.HashMap;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ReferenceVariableTest {
    @Test
    public void delegatesAndMutatesBothWaysTest() {
        StructVariable target = new StructVariable("here", new HashMap<>());
        target.putChild("x", Variable.value("1"));
        ReferenceVariable ref = new ReferenceVariable(target);

        assertEquals(ref.get(), "here");
        assertEquals(ref.resolve("x"), "1");
        assertEquals(ref.children(), target.children());

        assertTrue(ref.setChild("y", "2")); // mutate through the reference ...
        assertEquals(target.resolve("y"), "2"); // ... visible on the target
        assertTrue(target.setChild("z", "3")); // and mutate the target ...
        assertEquals(ref.resolve("z"), "3"); // ... visible through the reference
        assertTrue(ref.changed().isPresent());
    }

    @Test
    public void forkWrapsTheForkedTargetTest() {
        StructVariable target = new StructVariable("here", new HashMap<>());
        target.setChild("x", "1");
        ReferenceVariable ref = new ReferenceVariable(target);

        Variable forked = ref.fork();
        assertTrue(forked instanceof ReferenceVariable);
        assertTrue(forked.changed().isEmpty()); // fork resets tracking, same as the target's own fork()

        forked.setChild("x", "2");
        assertEquals(forked.resolve("x"), "2");
        assertEquals(target.resolve("x"), "1"); // original target untouched by mutating the fork
    }
}
