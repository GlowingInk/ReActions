package fun.reactions.logic.activity.flags;

import fun.reactions.logic.activity.Activity;
import fun.reactions.logic.environment.Environment;
import org.jetbrains.annotations.NotNull;

public interface Flag extends Activity {
    /**
     * Check the flag against current context
     * @param env activation context
     * @param paramsStr parameters of flag
     * @return is flag satisfied
     */
    @Override
    boolean proceed(@NotNull Environment env, @NotNull String paramsStr);
}
