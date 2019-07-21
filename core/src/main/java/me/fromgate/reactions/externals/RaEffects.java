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

package me.fromgate.reactions.externals;

import me.fromgate.playeffect.PlayEffect;
import me.fromgate.reactions.util.Param;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.location.Locator;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;


public class RaEffects {


	private static String effTypes = "smoke,flame,ender,potion";
	private static boolean use_play_effects = false;

	//ENDER_SIGNAL  POTION_BREAK MOBSPAWNER_FLAMES  SMOKE
	public static void init() {
		use_play_effects = isPlayEffectInstalled();
		if (use_play_effects) Msg.logMessage("PlayEffect found");
		else {
			Msg.logMessage("PlayEffect plugin is not found at your system");
			Msg.logMessage("If you need more effects please download PlayEffect from:");
			Msg.logMessage("http://dev.bukkit.org/bukkit-plugins/playeffect/");
		}
	}

	public static boolean isPlayEffectConnected() {
		return use_play_effects;
	}

	private static boolean isPlayEffectInstalled() {
		Plugin pe = Bukkit.getServer().getPluginManager().getPlugin("PlayEffect");
		if (pe == null) return false;
		return pe.getClass().getName().matches("^(me\\.fromgate\\.playeffect\\.PlayEffect){1}(Plugin){0,1}$");
	}

	private static Effect getEffectByName(String name) {
		if (name.equalsIgnoreCase("smoke")) return Effect.SMOKE;
		else if (name.equalsIgnoreCase("flame")) return Effect.MOBSPAWNER_FLAMES;
		else if (name.equalsIgnoreCase("ender")) return Effect.ENDER_SIGNAL;
		else if (name.equalsIgnoreCase("potion")) return Effect.POTION_BREAK;
		else return Effect.SMOKE;
	}

	public static void playEffect(Location loc, String eff, String param) {
		Param params = new Param(param);
		params.remove("param-line");
		playEffect(loc, eff, params);
	}

	public static void playEffect(Location loc, String eff, Param params) {
		if (use_play_effects) {
			params.set("loc", Locator.locationToString(loc));
			playPlayEffect(eff, params);
		} else {
			int data = params.isParamsExists("wind") ? parseSmokeDirection(params.getParam("wind")) : 9;
			playStandartEffect(loc, eff, data);
		}
	}

	public static void playEffect(Location loc, String eff, int data) {
		if (use_play_effects) {
			Param params = new Param();
			params.set("loc", Locator.locationToString(loc));
			playPlayEffect(eff, params);
		} else {
			playStandartEffect(loc, eff, data);
		}

	}

	public static void playStandartEffect(Location loc, String eff, int data) {
		int mod = data;
		World w = loc.getWorld();
		Effect effect = getEffectByName(eff);
		if (effect == null) return;
		if (eff.equalsIgnoreCase("smoke")) {
			if (mod < 0) mod = 0;
			if (mod > 8) mod = 8;
			if (data == 10) mod = Util.tryChance(9);
			if (data == 9) {
				for (int i = 0; i < 9; i++)
					w.playEffect(loc, Effect.SMOKE, i);

			} else w.playEffect(loc, Effect.SMOKE, mod);
		} else w.playEffect(loc, effect, mod);
	}

	private static void playPlayEffect(String eff, Param params) {
		PlayEffect.play(eff, params.getMap());
	}

	public static void playEffect(Player p, Param params) {
		String eff = params.getParam("eff", "");
		Location pLoc = p != null ? p.getLocation() : null;
		if (eff.isEmpty()) eff = params.getParam("type", "SMOKE"); // для совместимости со старыми версиями
		Location loc = Locator.parseLocation(params.getParam("loc", ""), pLoc);
		if (params.isParamsExists("loc")) params.set("loc", Locator.locationToString(loc));
		if (params.isParamsExists("loc1"))
			params.set("loc1", Locator.locationToString(Locator.parseLocation(params.getParam("loc1", ""), pLoc)));
		if (params.isParamsExists("loc2"))
			params.set("loc2", Locator.locationToString(Locator.parseLocation(params.getParam("loc2", ""), pLoc)));
		if (use_play_effects) {
			playPlayEffect(eff, params);
		} else {
			int modifier;
			int radius;
			if (!Util.isWordInList(eff, effTypes)) return;
			if (eff.equalsIgnoreCase("SMOKE")) modifier = parseSmokeDirection(params.getParam("dir", "random"));
			else modifier = Util.getMinMaxRandom(params.getParam("data", "0"));
			radius = params.getParam("radius", 0);
			boolean land = params.getParam("land", "true").equalsIgnoreCase("false");
			if (radius > 0) loc = Locator.getRadiusLocation(loc, radius, land);
			playStandartEffect(loc, eff, modifier);
		}
	}

	private static int parseSmokeDirection(String dir_str) {
		int d = 10;
		if (dir_str.equalsIgnoreCase("n")) {
			d = 7;
		}
		if (dir_str.equalsIgnoreCase("nw")) {
			d = 8;
		}
		if (dir_str.equalsIgnoreCase("ne")) {
			d = 6;
		}
		if (dir_str.equalsIgnoreCase("s")) {
			d = 1;
		}
		if (dir_str.equalsIgnoreCase("sw")) {
			d = 2;
		}
		if (dir_str.equalsIgnoreCase("se")) {
			d = 0;
		}
		if (dir_str.equalsIgnoreCase("w")) {
			d = 5;
		}
		if (dir_str.equalsIgnoreCase("e")) {
			d = 3;
		}
		if (dir_str.equalsIgnoreCase("calm")) {
			d = 4;
		}
		if (dir_str.equalsIgnoreCase("up")) {
			d = 4;
		}
		if (dir_str.equalsIgnoreCase("all")) {
			d = 9;
		}
		if (dir_str.equalsIgnoreCase("rnd")) {
			d = 10;
		}
		if (dir_str.equalsIgnoreCase("random")) {
			d = 10;
		}
		return d;
	}

}
