package me.fromgate.reactions.logic.activity.actions;

import me.fromgate.reactions.util.data.RaContext;
import org.jetbrains.annotations.NotNull;

public interface Action {
    boolean execute(@NotNull RaContext context, @NotNull String paramsStr);

    @NotNull String getName();

    boolean requiresPlayer();

    // TODO
    default boolean isAsync() {
        return true;
    }
}
