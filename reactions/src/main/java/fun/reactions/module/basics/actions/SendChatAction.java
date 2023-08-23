package fun.reactions.module.basics.actions;

import fun.reactions.model.activity.Activity;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author MaxDikiy
 * @since 04/10/2017
 */
@Aliased.Names({"CHAT", "CHAT_MESSAGE"})
public class SendChatAction implements Action, Activity.Personal {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull Player player, @NotNull String paramsStr) {
        if (paramsStr.startsWith("/")) {
            paramsStr = paramsStr.substring(1);
        }
        player.chat(paramsStr);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "SEND_CHAT";
    }
}
