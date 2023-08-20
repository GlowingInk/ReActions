package fun.reactions.module.basics.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

/**
 * @author MaxDikiy
 * @since 04/10/2017
 */
@Aliased.Names({"CHAT", "CHAT_MESSAGE"})
public class SendChatAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        if (env.getPlayer() != null) {
            String msg = paramsStr;
            msg = msg.replaceFirst("^[\\s/]+", "");
            env.getPlayer().chat(msg);
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "SEND_CHAT";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
