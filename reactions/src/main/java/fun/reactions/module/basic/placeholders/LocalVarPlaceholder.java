package fun.reactions.module.basic.placeholders;

import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variable;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names({"local", "local_var", "context"})
public class LocalVarPlaceholder implements Placeholder.Dynamic {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String phText) {
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

    @Override
    public @NotNull String getName() {
        return "local_variable";
    }
}
