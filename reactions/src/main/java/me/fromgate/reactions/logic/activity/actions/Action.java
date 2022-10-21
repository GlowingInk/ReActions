package me.fromgate.reactions.logic.activity.actions;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.Activity;
import org.jetbrains.annotations.NotNull;

public interface Action extends Activity {
    /**
     * Execute an action
     * @param context context of activation
     * @param paramsStr parameters of action
     * @return is action executed successfully
     */
    @Override
    boolean proceed(@NotNull RaContext context, @NotNull String paramsStr);
}
