package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import static ink.glowing.text.InkyMessage.inkyMessage;

@Aliased.Names({"RESPOND", "RESPONSE", "INKY", "IMSG"})
public class InkyMessageAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Audience receiver = env.getPlayer() == null
                ? env.getServer().getConsoleSender()
                : env.getPlayer();
        receiver.sendMessage(inkyMessage().deserialize(paramsStr));
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "INKYMESSAGE";
    }
}
