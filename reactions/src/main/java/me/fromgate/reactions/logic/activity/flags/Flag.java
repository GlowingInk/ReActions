package me.fromgate.reactions.logic.activity.flags;

import me.fromgate.reactions.logic.activity.Activity;
import me.fromgate.reactions.logic.context.Environment;
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
