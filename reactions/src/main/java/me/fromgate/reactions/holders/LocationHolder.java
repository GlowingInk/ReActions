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

package me.fromgate.reactions.holders;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.util.FileUtils;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.location.TpLocation;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LocationHolder {
    private static final Map<String, Location> locs = new HashMap<>();
    private static final Map<String, TpLocation> tports = new HashMap<>();

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
        if (tports.size() > 0) {
            File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "locations.yml");
            YamlConfiguration lcs = new YamlConfiguration();
            for (String key : tports.keySet()) {
                TpLocation tploc = tports.get(key);
                lcs.set(key + ".world", tploc.getWorld());
                lcs.set(key + ".x", tploc.getX());
                lcs.set(key + ".y", tploc.getY());
                lcs.set(key + ".z", tploc.getZ());
                lcs.set(key + ".yaw", tploc.getYaw());
                lcs.set(key + ".pitch", tploc.getPitch());
            }
            FileUtils.saveCfg(lcs, f, "Failed to save locations to configuration file");
        }
    }

    public static void loadLocs() {
        tports.clear();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "locations.yml");
        YamlConfiguration lcs = new YamlConfiguration();
        if (FileUtils.loadCfg(lcs, f, "Failed to load locations configuration file"))
            for (String key : lcs.getKeys(false))
                tports.put(key, new TpLocation(lcs.getString(key + ".world"),
                        lcs.getDouble(key + ".x"),
                        lcs.getDouble(key + ".y"),
                        lcs.getDouble(key + ".z"),
                        (float) lcs.getDouble(key + ".yaw"),
                        (float) lcs.getDouble(key + ".pitch")));
    }

    @SuppressWarnings("unused")
    public static boolean containsTpLoc(String locstr) {
        return tports.containsKey(locstr);
    }

    public static Location getTpLoc(String locstr) {
        if (tports.containsKey(locstr)) return tports.get(locstr).getLocation();
        return null;
    }

    public static int sizeTpLoc() {
        return tports.size();
    }

    public static boolean addTpLoc(String id, Location loc) {
        if (Utils.isStringEmpty(id)) return false;
        tports.put(id, new TpLocation(loc));
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
