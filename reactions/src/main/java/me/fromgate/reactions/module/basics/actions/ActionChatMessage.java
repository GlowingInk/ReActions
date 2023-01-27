package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.naming.Aliased;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-10-04.
 */
@Aliased.Names("CHAT")
public class ActionChatMessage implements Action {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String params) {
        if (context.getPlayer() != null) {
            String msg = params;
            msg = msg.replaceFirst("^[\\s/]+", "");
            context.getPlayer().chat(msg);
        }
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "CHAT_MESSAGE";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
