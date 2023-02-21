package me.fromgate.reactions.logic.activity.actions;

import me.fromgate.reactions.logic.environment.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Some actions can stop execution of activator
 */
public interface Stopper {
    /**
     * This method will be called after {@link Action#proceed(Environment, String)} if it returned {@code true}
     * @param actions List of actions that were stopped
     */
    void stop(@NotNull Environment env, @NotNull String params, @NotNull List<StoredAction> actions);
}
