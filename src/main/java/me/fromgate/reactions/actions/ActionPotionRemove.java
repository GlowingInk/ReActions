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

package me.fromgate.reactions.actions;

import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.parameter.Param;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ActionPotionRemove extends Action {

    @Override
    public boolean execute(RaContext context, Param params) {
        String str = removePotionEffect(context.getPlayer(), params.getParam("param-line", ""));
        if (str.isEmpty()) return false;
        this.setMessageParam(str);
        return true;
    }

    private String removePotionEffect(Player p, String param) {
        String str = "";
        if (param.equalsIgnoreCase("all") || param.equalsIgnoreCase("*"))
            for (PotionEffect pe : p.getActivePotionEffects()) p.removePotionEffect(pe.getType());
        else {
            String[] pefs = param.split(",");
            if (pefs.length > 0) {
                for (String pefStr : pefs) {
                    PotionEffectType pef = PotionEffectType.getByName(pefStr);
                    if (pef == null) continue;
                    if (p.hasPotionEffect(pef)) {
                        p.removePotionEffect(pef);
                        str = str.isEmpty() ? pef.getName() : str + ", " + pef.getName();
                    }
                }
            }
        }
        return str;

    }

}
