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

package me.fromgate.reactions.module.basics.flags;

import me.fromgate.reactions.externals.RaVault;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.naming.Aliased;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// TODO: Vault module
@Aliased.Names({"VAULT_MONEY", "BALANCE"})
public class FlagMoney implements Flag {
    @Override
    public @NotNull String getName() {
        return "MONEY";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    public boolean proceed(@NotNull Environment context, @NotNull String paramsStr) {
        if (!RaVault.isEconomyConnected()) return false;
        Parameters params = Parameters.fromString(paramsStr);
        Player player = context.getPlayer();
        double amount = params.getDouble("amount", () -> params.getDouble(Parameters.ORIGIN, -1));
        if (amount < 0) return false;
        String account = params.getString("player", player == null ? "" : player.getName());
        String world = params.getString("world");
        return RaVault.hasMoney(account, world, amount);
    }
}
