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

package me.fromgate.reactions;

import me.fromgate.reactions.storages.StoragesManager;
import me.fromgate.reactions.util.FileUtil;
import me.fromgate.reactions.util.Util;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;

public class Variables {
	// TODO: Something like classes and objects that just contains variables - actually just global variables

	// TODO: Why treemap?
	private static Map<String, String> vars = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	private final static Pattern VARP = Pattern.compile("(?i).*%varp?:\\S+%.*");

	private static String varId(Player player, String var) {
		return (player == null ? "general." + var : player.getName() + "." + var);
	}

	private static String varId(String player, String var) {
		return (player.isEmpty() ? "general." + var : player + "." + var);
	}

	public static void setVar(String player, String var, String value) {
		String prevVal = Variables.getVar(player, var, "");
		vars.put(varId(player, var), value);
		if (!Cfg.playerSelfVarFile) save();
		else save(player);
		StoragesManager.raiseVariableActivator(var, player, value, prevVal);
	}

	public static void setVar(Player player, String var, String value) {
		String prevVal = Variables.getVar(player, var, "");
		vars.put(varId(player, var), value);
		if (!Cfg.playerSelfVarFile) save();
		else save(player.getName());
		StoragesManager.raiseVariableActivator(var, player == null ? "" : player.getName(), value, prevVal);
	}

	public static void clearVar(Player player, String var) {
		String prevVal = Variables.getVar(player, var, "");
		String id = varId(player, var);
		vars.remove(id);
		if (!Cfg.playerSelfVarFile) save();
		else save(player.getName());
		StoragesManager.raiseVariableActivator(var, player == null ? "" : player.getName(), "", prevVal);
	}

	public static boolean clearVar(String player, String var) {
		String prevVal = Variables.getVar(player, var, "");
		String id = varId(player, var);
		if (!vars.containsKey(id)) return false;
		vars.remove(id);
		if (!Cfg.playerSelfVarFile) save();
		else save(player);
		StoragesManager.raiseVariableActivator(var, player, "", prevVal);
		return true;
	}


	public static String getVar(String player, String var, String defvar) {
		String id = varId(player, var);
		if (vars.containsKey(id)) return vars.get(id);
		return defvar;
	}

	public static String getVar(Player player, String var, String defvar) {
		String id = varId(player, var);
		if (vars.containsKey(id)) return vars.get(id);
		return defvar;
	}

	public static boolean cmpVar(String playerName, String var, String cmpvalue) {
		String id = varId(playerName, var);
		if (!vars.containsKey(id)) return false;
		String value = getVar(playerName, var, "");
		if (Util.isNumber(cmpvalue, value)) return (Double.parseDouble(cmpvalue) == Double.parseDouble(value));
		return value.equalsIgnoreCase(cmpvalue);
	}

	public static boolean cmpGreaterVar(String playerName, String var, String cmpvalue) {
		String id = varId(playerName, var);
		if (!vars.containsKey(id)) return false;
		if (!Util.isNumber(vars.get(id), cmpvalue)) return false;
		return Double.parseDouble(vars.get(id)) > Double.parseDouble(cmpvalue);
	}

	public static boolean cmpLowerVar(String playerName, String var, String cmpvalue) {
		String id = varId(playerName, var);
		if (!vars.containsKey(id)) return false;
		if (!Util.isNumber(vars.get(id), cmpvalue)) return false;
		return Double.parseDouble(vars.get(id)) < Double.parseDouble(cmpvalue);
	}

	public static boolean existVar(String playerName, String var) {
		return (vars.containsKey(varId(playerName, var)));
	}

	public static boolean incVar(Player player, String var) {
		return incVar(player, var, 1);
	}

	public static boolean incVar(String player, String var) {
		return incVar(player, var, 1);
	}

	public static boolean decVar(Player player, String var) {
		return incVar(player, var, -1);
	}

	public static boolean decVar(String player, String var) {
		return incVar(player, var, -1);
	}

	public static boolean incVar(Player player, String var, double addValue) {
		return incVar(player == null ? "" : player.getName(), var, addValue);
	}

	public static boolean incVar(String player, String var, double addValue) {
		String id = varId(player, var);
		if (!vars.containsKey(id)) setVar(player, var, "0");
		String valueStr = vars.get(id);
		if (!Util.isNumber(valueStr)) return false;
		setVar(player, var, String.valueOf(Double.parseDouble(valueStr) + addValue));
		return true;
	}


	public static boolean decVar(String player, String var, double decValue) {
		return incVar(player, var, decValue * (-1));
	}

	public static boolean decVar(Player player, String var, double decValue) {
		return incVar(player, var, decValue * (-1));
	}

	public static boolean mergeVar(Player player, String var, String stringToMerge, boolean spaceDivider) {
		String space = spaceDivider ? " " : "";
		String id = varId(player, var);
		if (!vars.containsKey(id)) setVar(player, var, "");
		setVar(player, var, getVar(player, var, "") + space + stringToMerge);
		return false;
	}


	public static void save() {
		YamlConfiguration cfg = new YamlConfiguration();
		File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "variables.yml");
		for (String key : vars.keySet())
			cfg.set(key, vars.get(key));
		FileUtil.saveCfg(cfg, f, "Failed to save variables configuration file");
	}

	public static void save(String player) {
		if (Cfg.playerAsynchSaveSelfVarFile) saveAsync(player);
		else savePlayer(player);
	}

	public static void savePlayer(String player) {
		YamlConfiguration cfg = new YamlConfiguration();
		String varDir = ReActions.getPlugin().getDataFolder() + File.separator + "variables";
		File dir = new File(varDir);
		if (!dir.exists() && !dir.mkdirs()) return;
		saveGeneral();
		if (player == null || player.isEmpty()) return;
		UUID id = Util.getUUID(player);
		if (id == null) return;
		File f = new File(varDir + File.separator + id.toString() + ".yml");
		for (String key : vars.keySet()) {
			if (key.contains(player)) cfg.set(key, vars.get(key));
		}
		if(FileUtil.saveCfg(cfg, f, "Failed to save variable configuration file"))
			removePlayerVars(player);
	}

	public static void saveAsync(String player) {
		JavaPlugin pluginInstance = ReActions.getPlugin();
		pluginInstance.getServer().getScheduler().runTaskAsynchronously(pluginInstance, () -> savePlayer(player));
	}

	private static void saveGeneral() {
		YamlConfiguration cfg = new YamlConfiguration();
		String varDir = ReActions.getPlugin().getDataFolder() + File.separator + "variables";
		File f = new File(varDir + File.separator + "general.yml");
		for (String key : vars.keySet())
			if (key.contains("general")) cfg.set(key, vars.get(key));
		FileUtil.saveCfg(cfg, f, "Failed to save variable configuration file");
	}

	public static void load() {
		vars.clear();
		try {
			YamlConfiguration cfg = new YamlConfiguration();
			File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "variables.yml");
			if (!f.exists()) return;
			cfg.load(f);
			for (String key : cfg.getKeys(true)) {
				if (!key.contains(".")) continue;
				vars.put(key, cfg.getString(key));
			}
			if (!Cfg.playerSelfVarFile) {
				loadVars();
				File dir = new File(ReActions.getPlugin().getDataFolder() + File.separator + "variables");
				if (!dir.exists() || !dir.isDirectory()) return;
				String[] files = dir.list();
				for (String file : files) {
					File fl = new File(dir, file);
					fl.delete();
				}
				dir.delete();
			}
		} catch (Exception ignored) {
		}
	}

	public static void loadVars() {
		if (Cfg.playerSelfVarFile) load();
		try {
			int deleted = 0;
			YamlConfiguration cfg = new YamlConfiguration();
			File dir = new File(ReActions.getPlugin().getDataFolder() + File.separator + "variables");
			if (!dir.exists()) return;
			for (File f : dir.listFiles()) {
				if (!f.isDirectory()) {
					if(f.length() == 0) {
						f.delete();
						deleted++;
						continue;
					}
					String fstr = f.getName();
					if (fstr.endsWith(".yml")) {
						cfg.load(f);
						for (String key : cfg.getKeys(true)) {
							if (key.contains(".")) vars.put(key, cfg.getString(key));
						}
					}
				}
			}
			Msg.logMessage("Deleted "+ deleted + " variable files.");
		} catch (Exception ignored) {}
	}

	private static void removePlayerVars(String player) {
		Map<String, String> varsTmp = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		YamlConfiguration cfg = new YamlConfiguration();
		String fileName = ReActions.getPlugin().getDataFolder() + File.separator + "variables.yml";
		File f = new File(fileName);
		if (!f.exists()) return;
		if(!FileUtil.loadCfg(cfg, f, "Failed to load variable file")) return;
		for (String key : cfg.getKeys(true)) {
			if (!key.contains(".")) continue;
			if (key.contains(player) || key.contains("general")) continue;
			varsTmp.put(key, cfg.getString(key));
		}

		YamlConfiguration cfg2 = new YamlConfiguration();
		for (String key : varsTmp.keySet())
			cfg2.set(key, varsTmp.get(key));
		if(!FileUtil.saveCfg(cfg2, f, "Failed to save variable file")) return;
		varsTmp.clear();
	}


	public static String replacePlaceholders(OfflinePlayer player, String str) {
		if (!VARP.matcher(str).matches()) return str;

		String newStr = str;
		for (String key : vars.keySet()) {
			String replacement = vars.get(key);
			replacement = Util.FLOAT_WITHZERO.matcher(replacement).matches() ? Integer.toString((int) Double.parseDouble(replacement)) : replacement; // Matcher.quoteReplacement(replacement);
			if (key.startsWith("general.")) {
				String id = key.substring(8); // key.replaceFirst("general\\.", "");
				newStr = newStr.replaceAll("(?i)%var:" + Pattern.quote(id) + "%", replacement);
			} else {
				if (player != null && key.matches("(?i)^" + player.getName() + "\\..*")) {
					String id = key.replaceAll("(?i)^" + player.getName() + "\\.", "");
					newStr = newStr.replaceAll("(?i)%varp:" + Pattern.quote(id) + "%", replacement);
				}
				newStr = newStr.replaceAll("(?i)%varp?:" + Pattern.quote(key) + "%", replacement);
			}
		}
		return newStr;
	}

	public static void printList(CommandSender sender, int pageNum, String mask) {
		int linesPerPage = (sender instanceof Player) ? 15 : 10000;
		List<String> varList = new ArrayList<>();
		for (String key : vars.keySet()) {
			if (mask.isEmpty() || key.contains(mask)) {
				varList.add(key + " : " + vars.get(key));
			}
		}
		Msg.printPage(sender, varList, Msg.MSG_VARLIST, pageNum, linesPerPage);
	}

	public static boolean matchVar(String playerName, String var, String value) {
		String id = varId(playerName, var);
		if (!vars.containsKey(id)) return false;
		String varValue = vars.get(id);
		return varValue.matches(value);
	}
}