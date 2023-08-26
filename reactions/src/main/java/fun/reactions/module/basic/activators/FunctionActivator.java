package fun.reactions.module.basic.activators;

import fun.reactions.model.Logic;
import fun.reactions.model.activators.ActivationContext;
import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.Variables;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

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

    @Deprecated
    public static class LegacyContext extends ActivationContext {
        private final Variables vars;

        public LegacyContext(Player player, Variables vars) {
            super(player);
            this.vars = vars;
        }

        @Override
        public @NotNull Class<? extends Activator> getType() {
            return FunctionActivator.class;
        }

        @Override
        protected @NotNull Map<String, Variable> prepareVariables() {
            return vars.forkMap();
        }
    }
}
