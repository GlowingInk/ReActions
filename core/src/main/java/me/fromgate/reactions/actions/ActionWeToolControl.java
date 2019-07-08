package me.fromgate.reactions.actions;

import me.fromgate.reactions.externals.worldedit.RaWorldEdit;
import me.fromgate.reactions.util.Param;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by MaxDikiy on 18/10/2017.
 */
public class ActionWeToolControl extends Action {
	@SuppressWarnings("deprecation")
	@Override
	public boolean execute(Player p, Param params) {
		Player player = p;
		boolean isToolControl;
		if (params.hasAnyParam("value", "player")) {
			String playerName = params.getParam("player", p != null ? p.getName() : "");
			isToolControl = params.getParam("value", false);
			player = playerName.isEmpty() ? null : Bukkit.getPlayerExact(playerName);
		} else isToolControl = params.getParam("param-line", false);

		RaWorldEdit.getSession(player).setToolControl(isToolControl);
		return true;
	}
}
