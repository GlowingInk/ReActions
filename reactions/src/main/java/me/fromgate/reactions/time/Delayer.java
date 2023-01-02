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

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.RaContext;
import me.fromgate.reactions.util.FileUtils;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class Delayer {
    // TODO: I don't like anything here...
    private static final DateFormat HH_MM_SS = new SimpleDateFormat("HH:mm:ss");

    private static final Map<String, Long> delays = new HashMap<>();

    private Delayer() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "delay.yml");
        for (String key : delays.keySet()) {
            long delayTime = delays.get(key);
            if (delayTime > System.currentTimeMillis())
                cfg.set(key, delayTime);
        }
        FileUtils.saveCfg(cfg, f, "Failed to save delays configuration file");
    }

    public static void load() {
        delays.clear();
        YamlConfiguration cfg = new YamlConfiguration();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "delay.yml");
        if (FileUtils.loadCfg(cfg, f, "Failed to load delay configuration file"))
            for (String key : cfg.getKeys(true)) {
                if (!key.contains(".")) continue;
                long delayTime = cfg.getLong(key);
                if (delayTime > System.currentTimeMillis())
                    delays.put(key, delayTime);
            }
    }

    public static boolean checkDelay(String id, long updateTime) {
        String idd = (id.contains(".") ? id : "global." + id);
        Long delay = delays.get(idd);
        boolean result = delay == null || delay < System.currentTimeMillis();
        if (result && updateTime > 0) Delayer.setDelay(idd, updateTime, false);
        return result;
    }

    public static boolean checkPersonalDelay(String playerName, String id, long updateTime) {
        return checkDelay(playerName + "." + id, updateTime);
    }

    public static void setDelay(String id, long delayTime, boolean add) {
        setDelaySave(id, delayTime, true, add);
    }

    public static void setDelaySave(String id, long delayTime, boolean save, boolean add) {
        String delayId = id.contains(".") ? id : "global." + id;
        long currentDelay = add && delays.containsKey(delayId) ? delays.get(delayId) : System.currentTimeMillis();
        delays.put(delayId, delayTime + currentDelay);
        if (save) save();
    }

    public static void setPersonalDelay(String playerName, String id, long delayTime, boolean add) {
        setDelay(playerName + "." + id, delayTime, add);
    }

    public static void printDelayList(CommandSender sender, int pageNum, int linePerPage) {
        Set<String> lst = new TreeSet<>();
        for (String key : delays.keySet()) {
            long delayTime = delays.get(key);
            if (delayTime < System.currentTimeMillis()) continue;
            String[] ln = key.split("\\.", 2);
            if (ln.length != 2) continue;
            lst.add("[" + ln[0] + "] " + ln[1] + ": " + TimeUtils.formatTime(delays.get(key)));
        }
        Msg.printPage(sender, lst, Msg.MSG_LISTDELAY, pageNum, linePerPage, true);
    }

    public static String[] getStringTime(String playerName, String id) {
        String fullId = (id.contains(".") ? id : (playerName == null || playerName.isEmpty() ? "global." + id : playerName + "." + id));
        if (checkDelay(fullId, 0)) return null;
        if (!delays.containsKey(fullId)) return null;
        long time = delays.get(fullId);
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

    public static void setTempPlaceholders(RaContext context, String playerName, String id) {
        String[] times = Delayer.getStringTime(playerName, id);
        if (times != null) {
            context.setVariable("delay-fulltime", times[0]);
            context.setVariable("delay-time", times[1]);
            context.setVariable("delay-left", times[7]);
            context.setVariable("delay-left-full", times[3]);
            context.setVariable("delay-left-hms", times[2]);
            context.setVariable("delay-left-hh", times[4]);
            context.setVariable("delay-left-mm", times[5]);
            context.setVariable("delay-left-ss", times[6]);
        }
    }

    private static String formatNum(long num) {
        return num > 9 ? Long.toString(num) : ("0" + num);
    }
}
