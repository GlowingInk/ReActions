package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;

@Aliased.Names({"MINI", "MMSG", "MINIM"})
public class MiniMessageAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Audience receiver = env.getPlayer() == null
                ? env.getServer().getConsoleSender()
                : env.getPlayer();
        receiver.sendMessage(miniMessage().deserialize(paramsStr.replace('§', '&')));
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "MINIMESSAGE";
    }
}
