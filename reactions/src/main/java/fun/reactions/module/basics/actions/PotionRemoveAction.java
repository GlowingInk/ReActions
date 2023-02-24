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
public class PotionRemoveAction implements Action {
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String content) {
        String str = removePotionEffect(env.getPlayer(), content);
        return !str.isEmpty();
    }

    @Override
    public @NotNull String getName() {
        return "POTION_REMOVE";
    }

    @Override
    public boolean requiresPlayer() {
        return true; // TODO Allow to use player selectors
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
