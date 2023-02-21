package me.fromgate.reactions.logic.activity.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.ActivatorsManager;
import me.fromgate.reactions.logic.activators.FunctionActivator;
import me.fromgate.reactions.logic.environment.Environment;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names({"FUNCT", "FUNCTION"})
public class RunFunctionAction implements Action {
    private final ReActions.Platform platform;

    public RunFunctionAction(@NotNull ReActions.Platform platform) {
        this.platform = platform;
    }

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        ActivatorsManager activators = platform.getActivators();
        String id = params.getString(params.findKey(Parameters.ORIGIN, "id", "activator", "exec"));
        Activator activator = activators.getActivator(id);
        if (activator == null) {
            Msg.logOnce("wrongact_" + id, "Failed to run FUNCTION activator " + id + ". Activator not found.");
            return false;
        } else {
            id = activator.getLogic().getName();
        }
        if (activator.getClass() != FunctionActivator.class) {
            Msg.logOnce("wrongactype_" + id, "Failed to run FUNCTION activator " + id + ". Wrong activator type.");
            return false;
        }
        env.getVariables().set("previous_activator", env.getActivatorName());
        try {
            activator.getLogic().executeLogic(new Environment(
                    id,
                    env.getVariables(),
                    env.getPlayer(),
                    env.isAsync()
            ));
        } catch (StackOverflowError error) {
            platform.logger().error(
                    "RUN_FUNCTION action failed in '" + env.getActivatorName() + "' due to stack overflow. " +
                    "Consider limiting the usage of looped FUNCTION actions or try using EXECUTE actions when possible."
            );
            return false;
        }
        return true;
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    public @NotNull String getName() {
        return "RUN_FUNCTION";
    }
}
