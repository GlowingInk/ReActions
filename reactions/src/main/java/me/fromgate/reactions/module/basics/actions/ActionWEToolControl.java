package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.externals.worldedit.RaWorldEdit;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 18/10/2017.
 */
public class ActionWEToolControl implements Action {
    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = context.getPlayer();
        boolean isToolControl = params.getBoolean("value", () -> params.getBoolean(Parameters.ORIGIN, false));
        if (params.contains("player"))
            player = Bukkit.getPlayerExact(params.getString("player"));
        RaWorldEdit.getSession(player).setToolControl(isToolControl);
        return true;
    }

    @Override
    public @NotNull String getName() {
        return "WE_TOOLCONTROL";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
