package fun.reactions.module.basics.actions;

import fun.reactions.logic.activity.actions.Action;
import fun.reactions.logic.environment.Environment;
import fun.reactions.module.basics.BasicModule;
import fun.reactions.util.naming.Aliased;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Aliased.Names("RESPOND")
public class ResponseAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String params) {
        Objects.requireNonNullElseGet(env.getPlayer(), Bukkit::getConsoleSender).sendMessage(BasicModule.getMineDown(params).toComponent());
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
