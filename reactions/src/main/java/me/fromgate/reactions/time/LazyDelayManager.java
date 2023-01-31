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

package me.fromgate.reactions.time;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.FileUtils;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.TreeSet;

public final class LazyDelayManager {
    // TODO: I don't like anything here...
    private static final DateFormat HH_MM_SS = new SimpleDateFormat("HH:mm:ss");

    private static final Object2LongMap<String> delays = new Object2LongOpenHashMap<>();

    private LazyDelayManager() {}

    public static void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "delay.yml");
        for (String key : delays.keySet()) {
            long delayTime = delays.getLong(key);
            if (delayTime > System.currentTimeMillis())
                cfg.set(key, delayTime);
        }
        FileUtils.saveCfg(cfg, f, "Failed to save delays configuration file");
    }

    public static void load() {
        delays.clear();
        YamlConfiguration cfg = new YamlConfiguration();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "delay.yml");
        if (FileUtils.loadCfg(cfg, f, "Failed to load delay configuration file")) {
            for (String key : cfg.getKeys(true)) {
                if (!key.contains(".")) continue;
                long delayTime = cfg.getLong(key);
                if (delayTime > System.currentTimeMillis()) {
                    delays.put(key, delayTime);
                }
            }
        }
    }

    public static boolean checkDelay(String id, long updateTime) {
        if (id.indexOf('.') == -1) id = "global." + id;
        long delay = delays.getOrDefault(id, -1);
        boolean result = delay < System.currentTimeMillis();
        if (result && updateTime > 0) setDelay(id, updateTime, false);
        return result;
    }

    public static boolean checkPersonalDelay(String playerName, String id, long updateTime) {
        return checkDelay(playerName + "." + id, updateTime);
    }

    public static void setDelay(String id, long delayTime, boolean add) {
        setDelaySave(id, delayTime, true, add);
    }

    public static void setDelaySave(String id, long delayTime, boolean save, boolean add) {
        if (id.indexOf('.') == -1) id = "global." + id;
        long currentDelay = add && delays.containsKey(id) ? delays.getLong(id) : System.currentTimeMillis();
        delays.put(id, delayTime + currentDelay);
        if (save) save();
    }

    public static void setPersonalDelay(String playerName, String id, long delayTime, boolean add) {
        setDelay(playerName + "." + id, delayTime, add);
    }

    public static void printDelayList(CommandSender sender, int pageNum, int linePerPage) {
        Set<String> lst = new TreeSet<>();
        for (String key : delays.keySet()) {
            long delayTime = delays.getLong(key);
            if (delayTime < System.currentTimeMillis()) continue;
            String[] ln = key.split("\\.", 2);
            if (ln.length != 2) continue;
            lst.add("[" + ln[0] + "] " + ln[1] + ": " + TimeUtils.formatTime(delays.getLong(key)));
        }
        Msg.printPage(sender, lst, Msg.MSG_LISTDELAY, pageNum, linePerPage, true);
    }

    public static String[] getStringTime(String playerName, String id) {
        String fullId = (id.contains(".") ? id : (playerName == null || playerName.isEmpty() ? "global." + id : playerName + "." + id));
        if (checkDelay(fullId, 0)) return null;
        if (!delays.containsKey(fullId)) return null;
        long time = delays.getLong(fullId);
        String[] times = new String[8];
        times[0] = TimeUtils.formatTime(time);
        times[1] = TimeUtils.formatTime(time, HH_MM_SS);
        time = time - System.currentTimeMillis();

        long sec = time / 1000;
        long min = sec / 60;
        sec = sec % 60;
        long hour = min / 60;
        min = min % 60;
        long days = hour / 24;
        hour = hour % 24;

        times[2] = formatNum(hour) + ":" + formatNum(min) + ":" + formatNum(sec);
        times[3] = days + "d " + times[3];
        times[4] = formatNum(hour);
        times[5] = formatNum(min);
        times[6] = formatNum(sec);
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append("d");
        if (days > 0 || hour > 0) sb.append(" ").append(hour).append("h");
        if (days > 0 || hour > 0 || min > 0) sb.append(" ").append(min).append("m");
        if (sb.length() == 0 || sec > 0) sb.append(" ").append(sec).append("s");
        times[7] = sb.toString().trim();
        return times;
    }

    public static void setTempPlaceholders(Environment context, String playerName, String id) {
        String[] times = LazyDelayManager.getStringTime(playerName, id);
        if (times != null) {
            context.getVariables().set("delay-fulltime", times[0]);
            context.getVariables().set("delay-time", times[1]);
            context.getVariables().set("delay-left", times[7]);
            context.getVariables().set("delay-left-full", times[3]);
            context.getVariables().set("delay-left-hms", times[2]);
            context.getVariables().set("delay-left-hh", times[4]);
            context.getVariables().set("delay-left-mm", times[5]);
            context.getVariables().set("delay-left-ss", times[6]);
        }
    }

    private static String formatNum(long num) {
        return num > 9 ? Long.toString(num) : ("0" + num);
    }
}
