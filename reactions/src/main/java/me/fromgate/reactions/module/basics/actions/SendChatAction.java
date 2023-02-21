package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.environment.Environment;
import me.fromgate.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-10-04.
 */
@Aliased.Names({"CHAT", "CHAT_MESSAGE"})
public class SendChatAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String params) {
        if (env.getPlayer() != null) {
            String msg = params;
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
