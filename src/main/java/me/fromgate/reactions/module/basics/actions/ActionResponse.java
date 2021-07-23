package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionResponse extends Action {
    @Override
    protected boolean execute(@NotNull RaContext context, @NotNull Parameters params) {
        Player player = context.getPlayer();
        if (player == null)
            Bukkit.getConsoleSender().sendMessage(params.toString());
        else if (player.isOnline()) player.sendMessage(params.toString());
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

    @Override
    protected boolean isParameterized() {
        return false;
    }
}
