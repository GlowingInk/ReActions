package fun.reactions.module.basic.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static ink.glowing.text.InkyMessage.inkyMessage;

@Aliased.Names("RESPOND")
public class ResponseAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Objects.requireNonNullElseGet(env.getPlayer(), Bukkit::getConsoleSender).sendMessage(inkyMessage().deserialize(paramsStr));
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "RESPONSE";
    }
}
