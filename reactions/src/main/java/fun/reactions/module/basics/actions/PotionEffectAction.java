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
import fun.reactions.util.NumberUtils;
import fun.reactions.util.NumberUtils.Is;
import fun.reactions.util.TimeUtils;
import fun.reactions.util.message.Msg;
import fun.reactions.util.naming.Aliased;
import fun.reactions.util.parameter.Parameters;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

@Aliased.Names("POTION")
public class PotionEffectAction implements Action {// TODO Allow to use player selectors
    @Override
    public boolean proceed(@NotNull Environment env, @NotNull String paramsStr) {
        Parameters params = Parameters.fromString(paramsStr);
        String str = potionEffect(env.getPlayer(), params);
        return !str.isEmpty();
    }

    @Override
    public @NotNull String getName() {
        return "POTION_EFFECT";
    }

    private String potionEffect(Player p, Parameters params) {
        if (params.isEmpty()) return "";
        String peffstr = "";
        int duration = 20;
        int amplifier = 1;
        boolean ambient = false;
        if (params.contains("param")) {
            String param = params.getString("param");
            if (param.isEmpty()) return "";
            if (param.contains("/")) {
                String[] prm = param.split("/");
                if (prm.length > 1) {
                    peffstr = prm[0];
                    if (NumberUtils.isNumber(prm[1], Is.POSITIVE_NATURAL)) duration = Integer.parseInt(prm[1]);
                    if ((prm.length > 2) && NumberUtils.isNumber(prm[2], Is.POSITIVE_NATURAL)) amplifier = Integer.parseInt(prm[2]);
                }
            } else peffstr = param;
        } else {
            peffstr = params.getString("type");
            duration = NumberUtils.compactLong(TimeUtils.timeToTicksSafe(TimeUtils.parseTime(params.getString("time", "3s"))));
            amplifier = Math.max(params.getInteger("level", 1) - 1, 0);
            ambient = params.getBoolean("ambient", false);
        }
        PotionEffectType pef = getPotionEffectType(peffstr);
        if  (pef == null) return "";

        PotionEffect pe = new PotionEffect(pef, duration, amplifier, ambient);
        if (p.hasPotionEffect(pef)) p.removePotionEffect(pef);
        p.addPotionEffect(pe);
        return pe.getType().getName() + ":" + pe.getAmplifier();
    }

    private static PotionEffectType getPotionEffectType(String potionEffect) {
        PotionEffectType pef = PotionEffectType.getByName(potionEffect);

        if (pef == null) {
            try {
                PotionType ptype = PotionType.valueOf(potionEffect.toUpperCase(Locale.ROOT));
                pef = ptype.getEffectType();
            } catch (IllegalArgumentException e) {
                Msg.logMessage("Unknown potion type name: " + potionEffect);
            }
        }

        return pef;
    }


}
