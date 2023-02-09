package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.module.basics.BasicModule;
import me.fromgate.reactions.util.naming.Aliased;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Aliased.Names("RESPOND")
public class ResponseAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String params) {
        Objects.requireNonNullElseGet(context.getPlayer(), Bukkit::getConsoleSender).sendMessage(BasicModule.getMineDown(params).toComponent());
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "RESPONSE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }
}
