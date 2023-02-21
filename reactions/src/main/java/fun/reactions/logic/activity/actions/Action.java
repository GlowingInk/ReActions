package fun.reactions.logic.activity.actions;

import fun.reactions.logic.activity.Activity;
import fun.reactions.logic.environment.Environment;
import org.jetbrains.annotations.NotNull;

public interface Action extends Activity {
    /**
     * Execute an action
     * @param env context of activation
     * @param paramsStr parameters of action
     * @return is action executed successfully
     */
    @Override
    boolean proceed(@NotNull Environment env, @NotNull String paramsStr);
}
