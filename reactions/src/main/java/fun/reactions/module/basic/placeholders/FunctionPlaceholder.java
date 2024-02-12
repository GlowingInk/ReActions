package fun.reactions.module.basic.placeholders;

import fun.reactions.model.activators.Activator;
import fun.reactions.model.environment.Environment;
import fun.reactions.model.environment.Variables;
import fun.reactions.module.basic.activators.FunctionActivator;
import fun.reactions.placeholders.Placeholder;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Aliased.Names("funct")
public class FunctionPlaceholder implements Placeholder {
    @Override
    public @Nullable String resolve(@NotNull Environment env, @NotNull String key, @NotNull String params) {
        Activator activator = env.getPlatform().getActivators().getActivator(params);
        if (activator == null || activator.getClass() != FunctionActivator.class) {
            return null;
        }
        String id = activator.getLogic().getName();
        Variables vars = new Variables();
        try {
            activator.getLogic().execute(new Environment(
                    env.getPlatform(),
                    id,
                    vars,
                    env.getPlayer(),
                    env.isAsync()
            ));
        } catch (StackOverflowError error) {
            env.getPlatform().logger().error(
                    "FUNCTION placeholder (" + params + ") failed in '" + env.getActivatorName() + "' due to stack overflow. " +
                    "Consider limiting the usage of looped FUNCTION placeholders and RUN_FUNCTION actions."
            );
        }
        return vars.getStringUnsafe("return");
    }

    @Override
    public @NotNull String getName() {
        return "function";
    }
}
