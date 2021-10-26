/*
 *  ReActions, Minecraft bukkit plugin
 *  (c)2012-2017, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/reactions/
 *   *
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
import me.fromgate.reactions.module.basics.*;
import me.fromgate.reactions.util.FileUtils;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.collections.CaseInsensitiveMap;
import me.fromgate.reactions.util.message.Msg;
import me.fromgate.reactions.util.parameter.Parameters;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TimersManager {
	/*
		Command example:
		/react add timer <name> activator:<exec> time:<HH:MM,HH:MM|0_0/5_*_*_*_?> [player:<player>] [world:<world>]


		Example Cron Expressions
		CronTrigger Example 1 - an expression to create a trigger that simply fires every 5 minutes
		"0 0/5 * * * ?"

		CronTrigger Example 2 - an expression to create a trigger that fires every 5 minutes, at 10 seconds after the
		minute (i.e. 10:00:10 am, 10:05:10 am, etc.).
		"10 0/5 * * * ?"

		CronTrigger Example 3 - an expression to create a trigger that fires at 10:30, 11:30, 12:30, and 13:30, on
		every Wednesday and Friday.

		"0 30 10-13 ? * WED,FRI"
		CronTrigger Example 4 - an expression to create a trigger that fires every half hour between the hours of 8 am
		and 10 am on the 5th and 20th of every month. Note that the trigger will NOT fire at 10:00 am, just at 8:00,
		8:30, 9:00 and 9:30
		"0 0/30 8-9 5,20 * ?"
		Note that some scheduling requirements are too complicated to express with a single trigger - such as "every 5
		minutes between 9:00 am and 10:00 am, and every 20 minutes between 1:00 pm and 10:00 pm". The solution in
		this scenario is to simply create two triggers, and register both of them to run the same job.

	 */

    // TODO: Timezones support

    private static final int LINES_PER_PAGE_15 = 15;
    private static BukkitTask ingameTimer = null;
    private static String currentIngameTime;
    private static BukkitTask serverTimer = null;
    private static Map<String, Timer> timers;
    private static Set<String> timersIngame;

    public static boolean addTimer(String name, Parameters params) {
        return addTimer(null, name, params, false);
    }

    public static void listTimers(CommandSender sender, int pageNum) {
        List<String> timerList = new ArrayList<>();
        Map<String, Timer> timers = getIngameTimers();
        for (String id : timers.keySet()) {
            Timer timer = timers.get(id);
            timerList.add((timer.isPaused() ? "&c" : "&2") + id + " &a" + timer);
        }
        timers = getServerTimers();
        for (String id : timers.keySet()) {
            Timer timer = timers.get(id);
            timerList.add((timer.isPaused() ? "&c" : "&2") + id + " &a" + timer);
        }
        Msg.printPage(sender, timerList, Msg.MSG_TIMERLIST, pageNum, LINES_PER_PAGE_15, true);
    }

    public static boolean removeTimer(CommandSender sender, String name) {
        if (name.isEmpty()) {
            Msg.MSG_TIMERNEEDNAME.print(sender);
            return false;
        }
        if (!timers.containsKey(name)) {
            Msg.MSG_TIMERUNKNOWNNAME.print(sender, name);
            return false;
        }
        timers.remove(name);
        save();
        return Msg.MSG_TIMERREMOVED.print(sender, name);
    }

    public static boolean addTimer(CommandSender sender, String name, Parameters params, boolean save) {
        if (name.isEmpty()) return false;
        if (timers.containsKey(name)) {
            Msg.MSG_TIMEREXIST.print(sender, name);
            return false;
        }
        if (params.isEmpty()) {
            Msg.MSG_TIMERNEEDPARAMS.print(sender);
            return false;
        }
        if (params.getString("activator", "").isEmpty()) {
            Msg.MSG_TIMERNEEDACTIVATOR.print(sender);
            return false;
        }
        if (!params.contains("timer-type")) {
            Msg.MSG_TIMERNEEDTYPE.print(sender);
            return false;
        }
        if (!params.contains("time")) {
            Msg.MSG_TIMERNEEDTIME.print(sender);
            return false;
        }
        Timer timer = new Timer(params);
        timers.put(name, timer);
        updateIngameTimers();
        if (save) save();
        return (sender == null) || Msg.MSG_TIMERADDED.print(sender, name);
    }

    public static Map<String, Timer> getIngameTimers() {
        Map<String, Timer> ingameTimers = new CaseInsensitiveMap<>();
        for (String key : timers.keySet()) {
            Timer timer = timers.get(key);
            if (timer.isIngameTimer()) ingameTimers.put(key, timer);
        }
        return ingameTimers;
    }

    public static Map<String, Timer> getServerTimers() {
        Map<String, Timer> serverTimers = new CaseInsensitiveMap<>();
        for (String key : timers.keySet()) {
            Timer timer = timers.get(key);
            if (!timer.isIngameTimer()) serverTimers.put(key, timer);
        }
        return serverTimers;
    }


    public static void updateIngameTimers() {
        timersIngame.clear();
        Map<String, Timer> ingame = getIngameTimers();
        for (String key : ingame.keySet()) {
            Timer timer = ingame.get(key);
            timersIngame.addAll(timer.getTimesIngame());
        }
    }


    public static void init() {
        currentIngameTime = "";
        timersIngame = new HashSet<>();
        timers = new CaseInsensitiveMap<>();
        load();
        initIngameTimer();
        initServerTimer();
    }

    public static void initIngameTimer() {
        if (ingameTimer != null) return;
        ingameTimer = Bukkit.getScheduler().runTaskTimerAsynchronously(ReActions.getPlugin(), () -> {
            String currentTime = TimeUtils.formattedIngameTime();
            if (currentIngameTime.equalsIgnoreCase(currentTime)) return;
            currentIngameTime = currentTime;
            if (!timersIngame.contains(currentIngameTime)) return;
            Map<String, Timer> timers = getIngameTimers();
            for (String key : timers.keySet()) {
                Timer timer = timers.get(key);
                if (timer.isTimeToRun()) {
                    Bukkit.getScheduler().runTask(ReActions.getPlugin(), () -> StoragesManager.triggerExec(null, timer.getParams()));
                }
            }
        }, 1, 4);
    }

    public static void initServerTimer() {
        if (serverTimer != null) return;
        serverTimer = Bukkit.getScheduler().runTaskTimerAsynchronously(ReActions.getPlugin(), () -> {
            for (Timer timer : getServerTimers().values()) {
                if (timer.isTimeToRun()) {
                    Bukkit.getScheduler().runTask(ReActions.getPlugin(), () -> StoragesManager.triggerExec(null, timer.getParams()));
                }
            }
        }, 1, 20);
    }

    public static void load() {
        timers.clear();
        YamlConfiguration cfg = new YamlConfiguration();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "timers.yml");
        if (FileUtils.loadCfg(cfg, f, "Failed to load timers.yml file"))
            for (String timerType : cfg.getKeys(false)) {
                if (!(timerType.equalsIgnoreCase("INGAME") || timerType.equalsIgnoreCase("SERVER"))) continue;
                ConfigurationSection cs = cfg.getConfigurationSection(timerType);
                if (cs == null) continue;
                for (String timerId : cs.getKeys(false)) {
                    ConfigurationSection csParams = cs.getConfigurationSection(timerId);
                    if (csParams == null) continue;
                    Parameters params = Parameters.fromString("");
                    params.put("timer-type", timerType);
                    for (String param : csParams.getKeys(true)) {
                        if (!csParams.isString(param)) continue;
                        params.put(param, csParams.getString(param));
                    }
                    addTimer(timerId, params);
                }
            }
    }

    public static void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "timers.yml");
        if (f.exists()) f.delete();
        for (String name : timers.keySet()) {
            Timer timer = timers.get(name);
            Parameters params = timer.getParams();
            if (params.isEmpty()) continue;
            String timerType = timer.isIngameTimer() ? "INGAME" : "SERVER";
            String root = timerType + "." + name + ".";
            for (String key : params.keySet()) {
                if (key.equalsIgnoreCase("timer-type")) continue;
                if (key.equalsIgnoreCase("origin-string")) continue;
                cfg.set(root + key, key.equalsIgnoreCase("time") ? params.getString(key).replace("_", " ") : params.getString(key));
            }
        }
        FileUtils.saveCfg(cfg, f, "Failed to save timers.yml file");
    }

    public static boolean isTimerExists(String timerName) {
        return timers.containsKey(timerName);
    }

    public static boolean setPause(String timerName, boolean pause) {
        if (timers.isEmpty()) return false;
        if (!(timerName.equalsIgnoreCase("all") || isTimerExists(timerName))) return false;
        if (timerName.equalsIgnoreCase("all")) {
            for (Timer timer : timers.values())
                timer.setPaused(pause);
        } else {
            Timer timer = timers.get(timerName);
            timer.setPaused(pause);
        }
        return true;
    }

    public static boolean isTimerWorking(String timerName) {
        if (!isTimerExists(timerName)) return false;
        return (!timers.get(timerName).isPaused());
    }

}

