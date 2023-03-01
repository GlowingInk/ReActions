package fun.reactions.module.basics.actions;

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.module.basics.ReActionsModule;
import fun.reactions.util.naming.Aliased;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Aliased.Names("RESPOND")
public class ResponseAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Objects.requireNonNullElseGet(env.getPlayer(), Bukkit::getConsoleSender).sendMessage(ReActionsModule.getMineDown(paramsStr).toComponent());
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
