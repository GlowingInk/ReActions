package fun.reactions.module.basics.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-10-04.
 */
@Aliased.Names({"CHAT", "CHAT_MESSAGE"})
public class SendChatAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        if (env.getPlayer() != null) {
            String msg = content;
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
