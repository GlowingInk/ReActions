package me.fromgate.reactions.logic.activators;

import me.fromgate.reactions.logic.Logic;
import me.fromgate.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"EXEC", "EXE", "EXECUTABLE"})
public class FunctionActivator extends Activator {
    private FunctionActivator(Logic base) {
        super(base);
    }

    public static Activator create(Logic base, Object ignored) {
        return new FunctionActivator(base);
    }

    @Override
    public boolean checkContext(@NotNull ActivationContext context) {
        return true;
    }
}
