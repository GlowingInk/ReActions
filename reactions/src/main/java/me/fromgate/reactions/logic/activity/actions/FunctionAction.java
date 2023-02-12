package me.fromgate.reactions.logic.activity.actions;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.Activator;
import me.fromgate.reactions.logic.activators.ActivatorsManager;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.module.basics.activators.ExecActivator;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("FUNCT")
public class FunctionAction implements Action {
    private final ReActions.Platform platform;

    public FunctionAction(@NotNull ReActions.Platform platform) {
        this.platform = platform;
    }

    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        ActivatorsManager activators = platform.getActivators();
        String id = params.getString(params.findKey(Parameters.ORIGIN, "activator", "exec"));
        Activator activator = activators.getActivator(id);
        if (activator == null) {
            Msg.logOnce("wrongact_" + id, "Failed to run EXEC activator " + id + ". Activator not found.");
            return false;
        } else {
            id = activator.getLogic().getName();
        }
        if (activator.getClass() != ExecActivator.class) {
            Msg.logOnce("wrongactype_" + id, "Failed to run EXEC activator " + id + ". Wrong activator type.");
            return false;
        }
        context.getVariables().set("previous_activator", context.getActivatorName());
        try {
            activator.getLogic().executeLogic(new Environment(
                    id,
                    context.getVariables(),
                    context.getPlayer(),
                    context.isAsync()
            ));
        } catch (StackOverflowError error) {
            platform.logger().error(
                    "FUNCTION action failed in '" + context.getActivatorName() + "' due to stack overflow. " +
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
        return "FUNCTION";
    }
}
