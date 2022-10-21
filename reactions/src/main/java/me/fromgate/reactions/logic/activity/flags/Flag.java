package me.fromgate.reactions.logic.activity.flags;

import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.Activity;
import org.jetbrains.annotations.NotNull;

public interface Flag extends Activity {
    /**
     * Check the flag against current context
     * @param context activation context
     * @param paramsStr parameters of flag
     * @return is flag satisfied
     */
    @Override
    boolean proceed(@NotNull RaContext context, @NotNull String paramsStr);
}
