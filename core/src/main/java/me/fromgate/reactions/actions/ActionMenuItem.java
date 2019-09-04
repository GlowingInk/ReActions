package me.fromgate.reactions.actions;

import me.fromgate.reactions.Variables;
import me.fromgate.reactions.menu.InventoryMenu;
import me.fromgate.reactions.util.parameter.Param;
import org.bukkit.entity.Player;

public class ActionMenuItem extends Action {

	@Override
	public boolean execute(Player player, Param params) {
		return InventoryMenu.createAndOpenInventory(player, params, Variables.getTempVars());
	}

}
