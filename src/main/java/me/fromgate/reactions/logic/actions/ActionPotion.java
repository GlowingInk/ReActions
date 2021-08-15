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

package me.fromgate.reactions.logic.actions;

import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.data.RaContext;
import me.fromgate.reactions.util.math.NumberUtils;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.Locale;

public class ActionPotion extends Action {

    @Override
    public boolean execute(RaContext context, Parameters params) {
        String str = potionEffect(context.getPlayer(), params);
        if (str.isEmpty()) return false;
        this.setMessageParam(str);
        return true;
    }


    private String potionEffect(Player p, Parameters params) {
        if (params.isEmpty()) return "";
        String peffstr = "";
        int duration = 20;
        int amplifier = 1;
        boolean ambient = false;
        if (params.contains("param")) {
            String param = params.getString("param", "");
            if (param.isEmpty()) return "";
            if (param.contains("/")) {
                String[] prm = param.split("/");
                if (prm.length > 1) {
                    peffstr = prm[0];
                    if (NumberUtils.isNonzeroInteger(prm[1])) duration = Integer.parseInt(prm[1]);
                    if ((prm.length > 2) && NumberUtils.isNonzeroInteger(prm[2])) amplifier = Integer.parseInt(prm[2]);
                }
            } else peffstr = param;
        } else {
            peffstr = params.getString("type", "");
            duration = NumberUtils.safeLongToInt(TimeUtils.timeToTicks(TimeUtils.parseTime(params.getString("time", "3s"))));
            amplifier = Math.max(params.getInteger("level", 1) - 1, 0);
            ambient = params.getBoolean("ambient", false);
        }
        PotionType ptype;
        try {
            ptype = PotionType.valueOf(peffstr.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException e){
            return "";
        }
        PotionEffectType pef = ptype.getEffectType();
        if (pef == null) {
            return "";
        }
        PotionEffect pe = new PotionEffect(pef, duration, amplifier, ambient);
        if (p.hasPotionEffect(pef)) p.removePotionEffect(pef);
        p.addPotionEffect(pe);
        return pe.getType().getName() + ":" + pe.getAmplifier();
    }


}
