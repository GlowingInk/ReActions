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

package fun.reactions.module.basics.actions;

import fun.reactions.externals.RaVault;
import fun.reactions.logic.activity.actions.Action;
import fun.reactions.logic.environment.Environment;
import fun.reactions.util.Rng;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Aliased.Names({"MONEYPAY", "MONEY_PAY"})
public class MoneyTakeAction implements Action {

    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        Player player = env.getPlayer();
        if (!RaVault.isEconomyConnected()) return false;
        if (params.size() == 0) return false;
        if (params.size() <= 2) params = parseOldFormat(player, params.origin());
        String amountStr = params.getString("amount");
        if (amountStr.isEmpty()) return false;
        String worldName = params.getString("world");
        String target = params.getString("target");
        String source = params.getString("source", params.getString("player", (player != null ? player.getName() : "")));
        if (source.isEmpty()) return false;
        String message = RaVault.debitAccount(source, target, amountStr, worldName);
        return !message.isEmpty();
    }

    @Override
    public @NotNull String getName() {
        return "MONEY_TAKE";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    private Parameters parseOldFormat(Player p, String mstr) {
        Map<String, String> newParams = new HashMap<>();
        if (p != null) newParams.put("source", p.getName());
        if (mstr.contains("/")) {
            String[] m = mstr.split("/");
            if (m.length >= 2) {
                newParams.put("amount", m[0].contains("-") ? Integer.toString(Rng.nextIntRanged(m[0])) : m[0]);
                newParams.put("target", m[1]);
            }
        } else newParams.put("amount", mstr.contains("-") ? Integer.toString(Rng.nextIntRanged(mstr)) : mstr);
        return Parameters.fromMap(newParams);
    }
}
