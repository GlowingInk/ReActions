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

package fun.reactions.holders;

import fun.reactions.ReActions;
import fun.reactions.util.FileUtils;
import fun.reactions.util.Utils;
import fun.reactions.util.location.position.RealPosition;
import fun.reactions.util.message.Msg;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LocationHolder {
    private static final Map<String, Location> locs = new HashMap<>();
    private static final Map<String, RealPosition> tports = new HashMap<>();

    private LocationHolder() {}

    public static void hold(Player p, Location loc) {
        if (p == null) return;
        if (loc == null) loc = p.getTargetBlock(null, 100).getLocation();
        locs.put(p.getName(), loc);
    }

    public static Location getHeld(Player p) {
        return locs.get(p.getName());
    }

    public static void saveLocs() {
        if (!tports.isEmpty()) {
            File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "locations.yml");
            YamlConfiguration lcs = new YamlConfiguration();
            for (var entry : tports.entrySet()) {
                entry.getValue().intoConfiguration(lcs.createSection(entry.getKey()));
            }
            FileUtils.saveCfg(lcs, f, "Failed to save locations to configuration file");
        }
    }

    public static void loadLocs() {
        tports.clear();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "locations.yml");
        YamlConfiguration lcs = new YamlConfiguration();
        if (FileUtils.loadCfg(lcs, f, "Failed to load locations configuration file"))
            for (String key : lcs.getKeys(false)) {
                ConfigurationSection locCfg = lcs.getConfigurationSection(key);
                if (locCfg == null) continue;
                tports.put(key, RealPosition.fromConfiguration(locCfg));
            }
    }

    public static Location getTpLoc(String locstr) {
        if (tports.containsKey(locstr)) return tports.get(locstr).toLocation();
        return null;
    }

    public static int sizeTpLoc() {
        return tports.size();
    }

    public static boolean addTpLoc(String id, Location loc) {
        if (Utils.isStringEmpty(id)) return false;
        tports.put(id, RealPosition.byLocation(loc));
        return true;
    }

    public static boolean removeTpLoc(String id) {
        return tports.remove(id) != null;
    }

    public static void printLocList(CommandSender sender, int pageNum, int linesPerPage) {
        List<String> locList = new ArrayList<>();
        for (String loc : tports.keySet()) {
            locList.add("&3" + loc + " &a" + tports.get(loc).toString());
        }
        Msg.printPage(sender, locList, Msg.MSG_LISTLOC, pageNum, linesPerPage, true);
    }
}
