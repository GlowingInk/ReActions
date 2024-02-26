package fun.reactions.module.basic.actions;

import fun.reactions.model.activators.Activator;
import fun.reactions.model.activators.ActivatorsManager;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.basic.activators.FunctionActivator;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"FUNCT", "FUNCTION"})
public class RunFunctionAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        ActivatorsManager activators = env.getPlatform().getActivators();
        String id = params.getString(params.findKey(Parameters.ORIGIN_KEY, "id", "activator", "exec"));
        Activator activator = activators.getActivator(id);
        if (activator == null) {
            env.warn("Failed to run FUNCTION activator " + id + ". Activator not found.");
            return false;
        } else {
            id = activator.getLogic().getName();
        }
        if (activator.getClass() != FunctionActivator.class) {
            env.warn("Failed to run FUNCTION activator " + id + ". Wrong activator type.");
            return false;
        }
        if (env.isStepAllowed()) {
            activator.getLogic().execute(new Environment(
                    env.getPlatform(),
                    id,
                    env.getVariables(),
                    env.getPlayer(),
                    env.getDepth() + 1,
                    env.isAsync()
            ));
        } else {
            env.getPlatform().logger().error(
                    "RUN_FUNCTION action in '" + id + "' was stopped at the depth '" + env.getDepth() + "' to prevent stack overflow. " +
                    "Consider limiting the usage of recursive RUN_FUNCTION actions and FUNCTION placeholders " +
                    "or try using EXECUTE actions."
            );
            return false;
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "RUN_FUNCTION";
    }
}
