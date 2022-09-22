package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public class ActionChange implements Action {
    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        // TODO: Error message
        return context.setChangeable(params.getString("key", params.getString("id")),
                params.getString("value"));
    }

    @Override
    public @NotNull String getName() {
        return "CHANGE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
