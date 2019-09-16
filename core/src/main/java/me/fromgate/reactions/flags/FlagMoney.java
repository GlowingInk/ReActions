/*  
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *
 *  This file is part of ReActions.
 *  
 *  ReActions is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ReActions is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with ReActions.  If not, see <http://www.gnorg/licenses/>.
 * 
 */

package me.fromgate.reactions.flags;

import me.fromgate.reactions.externals.RaEconomics;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Param;
import org.bukkit.entity.Player;

public class FlagMoney implements Flag {
	@Override
	public boolean checkFlag(RaContext context, String param) {
		Player player = context.getPlayer();
		if (!RaEconomics.isEconomyFound()) return false;
		Param params = new Param(param, "amount");
		String amountStr = params.getParam("amount", "a");
		if (!Util.isFloat(amountStr)) return false;
		double amount = Double.parseDouble(amountStr);
		String account = params.getParam("account", params.getParam("player", player == null ? "" : player.getName()));
		if (account.isEmpty()) return false;
		String currency = params.getParam("currency", "");
		String world = params.getParam("world", "");
		return RaEconomics.hasMoney(account, amount, currency, world);
	}
}
