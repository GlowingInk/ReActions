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

import me.fromgate.reactions.externals.RaEconomics;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.util.Alias;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.NumberUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

// TODO: Vault module
@Alias({"VAULT_MONEY", "BALANCE"})
public class FlagMoney extends Flag {
    @Override
    public @NotNull String getName() {
        return "MONEY";
    }

    @Override
    public boolean requiresPlayer() {
        return false;
    }

    @Override
    protected boolean check(@NotNull RaContext context, @NotNull Parameters params) {
        Player player = context.getPlayer();
        if (!RaEconomics.isEconomyFound()) return false;
        String amountStr = params.getString("amount", "a");
        if (!NumberUtils.isFloat(amountStr)) return false;
        double amount = Double.parseDouble(amountStr);
        String account = params.getString("account", params.getString("player", player == null ? "" : player.getName()));
        if (account.isEmpty()) return false;
        String world = params.getString("world", "");
        return RaEconomics.hasMoney(account, amount, world);
    }
}
