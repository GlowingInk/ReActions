package me.fromgate.reactions.module.basics.actions;

import de.themoep.minedown.adventure.MineDown;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Alias("RESPOND")
public class ActionResponse implements Action {
    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String params) {
        Objects.requireNonNullElseGet(context.getPlayer(), Bukkit::getConsoleSender).sendMessage(new MineDown(params).toComponent());
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
