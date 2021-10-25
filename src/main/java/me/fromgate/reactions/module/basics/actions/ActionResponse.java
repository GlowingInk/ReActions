package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ActionResponse extends Action {
    @Override
    public boolean execute(@NotNull RaContext context, @NotNull String params) {
        params = Msg.colorize(params);
        Player player = context.getPlayer();
        if (player == null)
            Bukkit.getConsoleSender().sendMessage(params);
        else player.sendMessage(params);
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
