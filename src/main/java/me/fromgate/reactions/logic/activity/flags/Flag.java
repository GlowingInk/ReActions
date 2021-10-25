package me.fromgate.reactions.logic.activity.flags;

import me.fromgate.reactions.util.data.RaContext;
import org.jetbrains.annotations.NotNull;

public abstract class Flag {
    public abstract boolean check(@NotNull RaContext context, @NotNull String paramsStr);

    public abstract @NotNull String getName();

    public abstract boolean requiresPlayer();

    // TODO
    protected boolean isAsync() {
        return true;
    }

    @Override
    public @NotNull String toString() {
        return getName();
    }
}
