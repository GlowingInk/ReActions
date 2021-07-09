package me.fromgate.reactions.module.defaults.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.jetbrains.annotations.NotNull;

public class ActionChange extends Action {
    @Override
    protected boolean execute(@NotNull RaContext context, @NotNull Parameters params) {
        // TODO: Error message
        return context.setChangeable(params.getString("key", params.getString("id")),
                params.getString("value", ""));
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
