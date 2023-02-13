package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.logic.context.Variables;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"LOCAL_VAR_BATCH", "LOCAL_VAR_MULTIPLE"})
public class LocalVarBulkAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        if (params.isEmpty()) return false;
        Variables vars = context.getVariables();
        params.forEach(vars::set);
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
