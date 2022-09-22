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

import me.fromgate.reactions.module.basics.StoragesManager;
import me.fromgate.reactions.util.FileUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.collections.CaseInsensitiveMap;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VariablesManager {

    // TODO: Something like classes and objects that just contains variables - actually just global variables
    private final Map<String, String> vars;

    public VariablesManager() {
        this.vars = new CaseInsensitiveMap<>();
    }

    public String getVariable(String player, String var) {
        return vars.get(formatId(player, var));
    }

    public void setVariable(String player, String var, String value) {
        String prevVal = vars.put(formatId(player, var), value);
        if (!Cfg.playerSelfVarFile) save();
        else save(player);
        StoragesManager.triggerVariable(var, player, value, prevVal == null ? "" : prevVal);
    }

    public boolean removeVariable(String player, String var) {
        String id = formatId(player, var);
        String prevVal = vars.remove(id);
        if (prevVal == null) return false;
        if (!Cfg.playerSelfVarFile) save();
        else save(player);
        StoragesManager.triggerVariable(var, player, "", prevVal);
        return true;
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "variables.yml");
        for (String key : vars.keySet())
            cfg.set(key, vars.get(key));
        FileUtils.saveCfg(cfg, f, "Failed to save variables configuration file");
    }

    public void save(String player) {
        if (Cfg.playerAsynchSaveSelfVarFile) saveAsync(player);
        else savePlayer(player);
    }

    public void savePlayer(String player) {
        YamlConfiguration cfg = new YamlConfiguration();
        String varDir = ReActions.getPlugin().getDataFolder() + File.separator + "variables";
        File dir = new File(varDir);
        if (!dir.exists() && !dir.mkdirs()) return;
        saveGeneral();
        if (player == null || player.isEmpty()) return;
        UUID id = Utils.getUUID(player);
        if (id == null) return;
        File f = new File(varDir + File.separator + id + ".yml");
        for (String key : vars.keySet()) {
            if (key.contains(player)) cfg.set(key, vars.get(key));
        }
        if (FileUtils.saveCfg(cfg, f, "Failed to save variable configuration file"))
            removePlayerVars(player);
    }

    public void saveAsync(String player) {
        Bukkit.getScheduler().runTaskAsynchronously(ReActions.getPlugin(), () -> savePlayer(player));
    }

    private void saveGeneral() {
        YamlConfiguration cfg = new YamlConfiguration();
        String varDir = ReActions.getPlugin().getDataFolder() + File.separator + "variables";
        File f = new File(varDir + File.separator + "general.yml");
        for (String key : vars.keySet())
            if (key.contains("general")) cfg.set(key, vars.get(key));
        FileUtils.saveCfg(cfg, f, "Failed to save variable configuration file");
    }

    public void load() {
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

    public void loadVars() {
        if (Cfg.playerSelfVarFile) load();
        try {
            int deleted = 0;
            YamlConfiguration cfg = new YamlConfiguration();
            File dir = new File(ReActions.getPlugin().getDataFolder() + File.separator + "variables");
            if (!dir.exists()) return;
            for (File f : dir.listFiles()) {
                BasicFileAttributes fileAttributes = Files.readAttributes(f.toPath(), BasicFileAttributes.class);
                if (!fileAttributes.isDirectory()) {
                    if (fileAttributes.size() == 0) {
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
            Msg.logMessage("Deleted " + deleted + " variable files.");
        } catch (Exception ignored) {
        }
    }

    private void removePlayerVars(String player) {
        Map<String, String> varsTmp = new CaseInsensitiveMap<>();
        YamlConfiguration cfg = new YamlConfiguration();
        String fileName = ReActions.getPlugin().getDataFolder() + File.separator + "variables.yml";
        File f = new File(fileName);
        if (!f.exists()) return;
        if (!FileUtils.loadCfg(cfg, f, "Failed to load variable file")) return;
        for (String key : cfg.getKeys(true)) {
            if (!key.contains(".")) continue;
            if (key.contains(player) || key.contains("general")) continue;
            varsTmp.put(key, cfg.getString(key));
        }

        YamlConfiguration cfg2 = new YamlConfiguration();
        for (String key : varsTmp.keySet())
            cfg2.set(key, varsTmp.get(key));
        if (!FileUtils.saveCfg(cfg2, f, "Failed to save variable file")) return;
        varsTmp.clear();
    }

    public void printList(CommandSender sender, int pageNum, String mask) {
        int linesPerPage = (sender instanceof Player) ? 15 : 10000;
        List<String> varList = new ArrayList<>();
        for (String key : vars.keySet()) {
            if (mask.isEmpty() || key.contains(mask)) {
                varList.add(key + " : " + vars.get(key));
            }
        }
        Msg.printPage(sender, varList, Msg.MSG_VARLIST, pageNum, linesPerPage);
    }

    private static String formatId(String player, String var) {
        return (Utils.isStringEmpty(player) ? "general" : player) + "." + var;
    }
}
