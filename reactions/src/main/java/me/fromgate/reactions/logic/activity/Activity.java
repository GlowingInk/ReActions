package me.fromgate.reactions.logic.activity;

import me.fromgate.reactions.logic.RaContext;
import org.jetbrains.annotations.NotNull;

public interface Activity {
    boolean proceed(@NotNull RaContext context, @NotNull String paramsStr);

    @NotNull String getName();

    boolean requiresPlayer();

    // TODO
    default boolean isAsync() {
        return true;
    }
}
