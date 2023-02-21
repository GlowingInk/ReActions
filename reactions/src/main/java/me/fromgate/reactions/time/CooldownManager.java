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
import me.fromgate.reactions.logic.environment.Environment;
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

public final class CooldownManager { // TODO Requires refactoring
    private static final DateFormat HH_MM_SS = new SimpleDateFormat("HH:mm:ss");

    private static final Object2LongMap<String> cooldowns = new Object2LongOpenHashMap<>();

    private CooldownManager() {}

    public static void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "delay.yml");
        for (String key : cooldowns.keySet()) {
            long delayTime = cooldowns.getLong(key);
            if (delayTime > System.currentTimeMillis())
                cfg.set(key, delayTime);
        }
        FileUtils.saveCfg(cfg, f, "Failed to save delays configuration file");
    }

    public static void load() {
        cooldowns.clear();
        YamlConfiguration cfg = new YamlConfiguration();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "delay.yml");
        if (FileUtils.loadCfg(cfg, f, "Failed to load delay configuration file")) {
            for (String key : cfg.getKeys(true)) {
                if (!key.contains(".")) continue;
                long delayTime = cfg.getLong(key);
                if (delayTime > System.currentTimeMillis()) {
                    cooldowns.put(key, delayTime);
                }
            }
        }
    }

    public static boolean checkCooldown(String id, long updateTime) {
        if (id.indexOf('.') == -1) id = "global." + id;
        long delay = cooldowns.getOrDefault(id, -1);
        boolean result = delay < System.currentTimeMillis();
        if (result && updateTime > 0) setCooldown(id, updateTime, false);
        return result;
    }

    public static boolean checkPersonalCooldown(String playerName, String id, long updateTime) {
        return checkCooldown(playerName + "." + id, updateTime);
    }

    public static void setCooldown(String id, long delayTime, boolean add) {
        setCooldownSave(id, delayTime, true, add);
    }

    public static void setCooldownSave(String id, long delayTime, boolean save, boolean add) {
        if (id.indexOf('.') == -1) id = "global." + id;
        long currentDelay = add && cooldowns.containsKey(id) ? cooldowns.getLong(id) : System.currentTimeMillis();
        cooldowns.put(id, delayTime + currentDelay);
        if (save) save();
    }

    public static void setPersonalCooldown(String playerName, String id, long delayTime, boolean add) {
        setCooldown(playerName + "." + id, delayTime, add);
    }

    public static void printCooldownList(CommandSender sender, int pageNum, int linePerPage) {
        Set<String> lst = new TreeSet<>();
        for (String key : cooldowns.keySet()) {
            long delayTime = cooldowns.getLong(key);
            if (delayTime < System.currentTimeMillis()) continue;
            String[] ln = key.split("\\.", 2);
            if (ln.length != 2) continue;
            lst.add("[" + ln[0] + "] " + ln[1] + ": " + TimeUtils.formatTime(cooldowns.getLong(key)));
        }
        Msg.printPage(sender, lst, Msg.MSG_LISTDELAY, pageNum, linePerPage, true);
    }

    public static String[] getStringTime(String playerName, String id) {
        String fullId = (id.contains(".") ? id : (playerName == null || playerName.isEmpty() ? "global." + id : playerName + "." + id));
        if (checkCooldown(fullId, 0)) return null;
        if (!cooldowns.containsKey(fullId)) return null;
        long time = cooldowns.getLong(fullId);
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

    public static void setTempPlaceholders(Environment env, String playerName, String id) { // TODO Make vars lazy
        String[] times = CooldownManager.getStringTime(playerName, id);
        if (times != null) {
            env.getVariables().set("cooldown-fulltime", times[0]);
            env.getVariables().set("cooldown-time", times[1]);
            env.getVariables().set("cooldown-left", times[7]);
            env.getVariables().set("cooldown-left-full", times[3]);
            env.getVariables().set("cooldown-left-hms", times[2]);
            env.getVariables().set("cooldown-left-hh", times[4]);
            env.getVariables().set("cooldown-left-mm", times[5]);
            env.getVariables().set("cooldown-left-ss", times[6]);

            // TODO Remove legacy
            env.getVariables().set("delay-fulltime", times[0]);
            env.getVariables().set("delay-time", times[1]);
            env.getVariables().set("delay-left", times[7]);
            env.getVariables().set("delay-left-full", times[3]);
            env.getVariables().set("delay-left-hms", times[2]);
            env.getVariables().set("delay-left-hh", times[4]);
            env.getVariables().set("delay-left-mm", times[5]);
            env.getVariables().set("delay-left-ss", times[6]);
        }
    }

    private static String formatNum(long num) {
        return num > 9 ? Long.toString(num) : ("0" + num);
    }
}
