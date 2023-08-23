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

import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Environment;
import fun.reactions.util.naming.Aliased;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

@Aliased.Names("RMVPOT")
public class PotionRemoveAction implements Action { // TODO Allow to use player selectors
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        String str = removePotionEffect(env.getPlayer(), paramsStr);
        return !str.isEmpty();
    }

    @Override
    public @NotNull String getName() {
        return "POTION_REMOVE";
    }

    private String removePotionEffect(Player player, String param) {
        String str = "";
        if (param.equalsIgnoreCase("all") || param.equalsIgnoreCase("*"))
            for (PotionEffect pe : player.getActivePotionEffects()) player.removePotionEffect(pe.getType());
        else {
            String[] pefs = param.split(",");
            if (pefs.length > 0) {
                for (String pefStr : pefs) {
                    PotionEffectType pef = PotionEffectType.getByName(pefStr);
                    if (pef == null) continue;
                    if (player.hasPotionEffect(pef)) {
                        player.removePotionEffect(pef);
                        str = str.isEmpty() ? pef.getName() : str + ", " + pef.getName();
                    }
                }
            }
        }
        return str;

    }

}
