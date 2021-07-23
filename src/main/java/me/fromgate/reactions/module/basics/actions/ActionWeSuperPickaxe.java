package me.fromgate.reactions.module.basics.actions;

import me.fromgate.reactions.externals.worldedit.RaWorldEdit;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Created by MaxDikiy on 18/10/2017.
 */
public class ActionWeSuperPickaxe extends Action {
    @Override
    protected boolean execute(@NotNull RaContext context, @NotNull Parameters params) {
        Player player = context.getPlayer();
        boolean isSP = params.getBoolean("value", params.getBoolean("param-line", false));
        if (params.contains("player"))
            player = Utils.getPlayerExact(params.getString("player"));
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
