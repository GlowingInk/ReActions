package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"var_temp", "var_temp_set", "temp_var", "temp_variable", "var_local", "change"})
public class LocalVariableAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        context.getVariables().set(params.getStringSafe("id", () -> params.getString("key")), params.getString("value"));
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "LOCAL_VAR";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
