package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"var_temp", "var_temp_set", "temp_var", "temp_variable", "var_local", "change"})
public class LocalVarAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        env.getVariables().set(
                params.getString(params.findKey("key", "id")),
                params.getString("value", params.getBoolean("delete-empty", false) ? null : "")
        );
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "LOCAL_VAR";
    }
}
