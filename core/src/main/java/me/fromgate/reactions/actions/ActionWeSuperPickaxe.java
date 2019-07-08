package me.fromgate.reactions.actions;

import me.fromgate.reactions.externals.worldedit.RaWorldEdit;
import me.fromgate.reactions.util.Param;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by MaxDikiy on 18/10/2017.
 */
public class ActionWeSuperPickaxe extends Action {
    @SuppressWarnings("deprecation")
    @Override
    public boolean execute(Player p, Param params) {
        Player player = p;
        boolean isSP;
        if (params.hasAnyParam("value", "player")) {
            String playerName = params.getParam("player", p != null ? p.getName() : "");
            isSP = params.getParam("value", false);
            player = playerName.isEmpty() ? null : Bukkit.getPlayerExact(playerName);
        } else isSP = params.getParam("param-line", false);

        if (isSP) RaWorldEdit.getSession(player).enableSuperPickAxe();
        else RaWorldEdit.getSession(player).disableSuperPickAxe();
        return true;

    }
}
