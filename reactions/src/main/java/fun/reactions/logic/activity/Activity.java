package fun.reactions.logic.activity;

import fun.reactions.logic.environment.Environment;
import fun.reactions.util.naming.Named;
import org.jetbrains.annotations.NotNull;

public interface Activity extends Named {
    boolean proceed(@NotNull Environment env, @NotNull String paramsStr);

    boolean requiresPlayer();

    // TODO
    default boolean isAsync() {
        return true;
    }
}
