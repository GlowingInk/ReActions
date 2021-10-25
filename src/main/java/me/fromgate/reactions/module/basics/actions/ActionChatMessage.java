package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 2017-10-04.
 */
@Alias("CHAT")
public class ActionChatMessage extends Action {
    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String params) {
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
