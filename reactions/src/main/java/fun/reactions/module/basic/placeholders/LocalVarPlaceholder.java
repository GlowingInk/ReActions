package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variable;
import fun.reactions.model.environment.Variables;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names({"local", "local_var", "context"})
public class LocalVarPlaceholder implements Placeholder.Dynamic {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String phText) {
        if (phText.endsWith("?")) {
            return Boolean.toString(exists(env.getVariables(), phText.substring(0, phText.length() - 1)));
        }
        int index = phText.indexOf(':');
        if (index == -1) {
            return env.getVariables().getStringUnsafe(phText);
        }
        String key = phText.substring(0, index);
        Variable var = env.getVariables().getVariable(key);
        if (var == null) {
            return env.getVariables().getStringUnsafe(phText);
        }
        return var.resolve(phText.substring(index + 1));
    }

    private static boolean exists(@NotNull Variables vars, @NotNull String path) {
        int index = path.indexOf(':');
        if (index == -1) return vars.getVariable(path) != null;
        Variable var = vars.getVariable(path.substring(0, index));
        return var == null ? vars.getVariable(path) != null : var.has(path.substring(index + 1));
    }

    @Override
    public @NotNull String getName() {
        return "local_variable";
    }
}
