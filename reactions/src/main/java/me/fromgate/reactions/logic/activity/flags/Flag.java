package me.fromgate.reactions.logic.activity.flags;

import me.fromgate.reactions.logic.RaContext;
import org.jetbrains.annotations.NotNull;

public interface Flag {
    boolean check(@NotNull RaContext context, @NotNull String paramsStr);

    @NotNull String getName();

    boolean requiresPlayer();

    // TODO
    default boolean isAsync() {
        return true;
    }
}
