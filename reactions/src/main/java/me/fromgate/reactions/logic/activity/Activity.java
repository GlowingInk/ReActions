package me.fromgate.reactions.logic.activity;

import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.naming.Named;
import org.jetbrains.annotations.NotNull;

public interface Activity extends Named {
    boolean proceed(@NotNull Environment context, @NotNull String paramsStr);

    boolean requiresPlayer();

    // TODO
    default boolean isAsync() {
        return true;
    }
}
