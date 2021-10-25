package me.fromgate.reactions.logic.activity.actions;

import me.fromgate.reactions.util.data.RaContext;
import org.jetbrains.annotations.NotNull;

public abstract class Action {
    public abstract boolean execute(@NotNull RaContext context, @NotNull String paramsStr);

    @NotNull
    public abstract String getName();

    public abstract boolean requiresPlayer();

    // TODO
    protected boolean isAsync() {
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }
}
