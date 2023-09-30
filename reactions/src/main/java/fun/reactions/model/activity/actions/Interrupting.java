package fun.reactions.model.activity.actions;

import fun.reactions.model.environment.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Some actions can stop execution of activator
 */
public interface Interrupting {
    /**
     * This method will be called after {@link Action#proceed(Environment, String)} if it returned {@code true}
     * @param remaining List of actions that were stopped
     */
    void stop(@NotNull Environment env, @NotNull String params, @NotNull List<Action.Stored> remaining);
}
