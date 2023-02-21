package fun.reactions.module.basics.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"EXEC", "EXE", "EXECUTABLE", "FUNCT"})
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
