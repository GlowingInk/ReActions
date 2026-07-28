package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.Variables;
import fun.reactions.model.environment.variables.StructVariable;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

@Aliased.Names({"var_temp", "var_temp_set", "temp_var", "temp_variable", "var_local", "change"})
public class LocalVarAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        String key = params.getString(params.findKey("key", "id"));
        if (params.contains("children")) {
            env.getVariables().setVariable(key, StructVariable.fromParameters(params));
            return true;
        }
        int index = key.indexOf(':');
        if (index != -1) {
            Variables vars = env.getVariables();
            String rootKey = key.substring(0, index);
            Variable root = vars.getVariable(rootKey);
            if (root == null) {
                root = new StructVariable("", new HashMap<>());
                vars.setVariable(rootKey, root);
            }
            root.setChild(key.substring(index + 1), params.getString("value", ""));
            return true;
        }
        env.getVariables().set(
                key,
                params.getString("value", params.getBoolean("delete-empty", false) ? null : "")
        );
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "LOCAL_VAR";
    }
}
