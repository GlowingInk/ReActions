package fun.reactions.module.basics.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"LOCAL_VAR_BATCH", "LOCAL_VAR_MULTIPLE"})
public class LocalVarBulkAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        if (params.isEmpty()) return false;
        params.forEach(env.getVariables()::set);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "LOCAL_VAR_BULK";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
