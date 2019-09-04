package me.fromgate.reactions.flags;

import me.fromgate.reactions.Variables;
import me.fromgate.reactions.util.Util;
import org.bukkit.entity.Player;

/**
 * Created by MaxDikiy on 5/4/2017.
 */
public class FlagWalkSpeed implements Flag {
	@Override
	public boolean checkFlag(Player player, String param) {
		if (!Util.isInteger(param)) return false;
		long walkSpeed = Math.round(player.getWalkSpeed() * 10);
		Variables.setTempVar("walkspeed", Long.toString(walkSpeed));
		return walkSpeed >= Integer.parseInt(param);

	}
}
