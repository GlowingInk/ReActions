package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.externals.worldedit.RaWorldEdit;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 18/10/2017.
 */
public class ActionWESuperPickaxe implements Action {
    @Override
    public boolean proceed(@NotNull RaContext context, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = context.getPlayer();
        boolean isSP = params.getBoolean("value", () -> params.getBoolean(Parameters.ORIGIN_KEY, false));
        if (params.contains("player"))
            player = Bukkit.getPlayerExact(params.getString("player"));
        if (isSP) RaWorldEdit.getSession(player).enableSuperPickAxe();
        else RaWorldEdit.getSession(player).disableSuperPickAxe();
        return true;

    }

    @Override
    public @NotNull String getName() {
        return "WE_SUPERPICKAXE";
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }
}
