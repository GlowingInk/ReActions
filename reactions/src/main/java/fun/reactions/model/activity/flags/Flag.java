package fun.reactions.model.activity.flags;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.environment.Environment;
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
