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

package me.fromgate.reactions.activators;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.event.RAEvent;
import me.fromgate.reactions.externals.RaWorldGuard;
import me.fromgate.reactions.timer.Timers;
import me.fromgate.reactions.util.ActVal;
import me.fromgate.reactions.util.FlagVal;
import me.fromgate.reactions.util.message.Msg;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Activators {
    private static ReActions plg() {
        return ReActions.instance;
    }

    private static List<Activator> act;
    private static Set<String> stopexec;


    public static void init() {
        act = new ArrayList<>();
        stopexec = new HashSet<>();
        loadActivators();
    }

    public static void loadActivators() {
        List<String> groups = findGroupsInDirs("");
        if (!groups.isEmpty())
            for (String group : groups)
                loadActivators(group, false);

        Timers.updateIngameTimers();
        RaWorldGuard.updateRegionCache();
    }

    private static List<String> findGroupsInDir() {
        List<String> grps = new ArrayList<>();
        File dir = new File(plg().getDataFolder() + File.separator + "Activators" + File.separator);
        if (!dir.exists()) dir.mkdirs();
        for (String fstr : dir.list())
            if (fstr.endsWith(".yml")) {
                grps.add(fstr.substring(0, fstr.length() - 4));
            }
        return grps;
    }

    private static List<String> findGroupsInDirs(String dir) {
        dir = ((dir.isEmpty()) ? "" : dir + File.separator);
        List<String> grps = new ArrayList<>();
        File dirs = new File(plg().getDataFolder() + File.separator + "Activators" + File.separator + dir);
        if (!dirs.exists()) dirs.mkdirs();
        for (File f : dirs.listFiles()) {
            if (f.isDirectory()) {
                grps.addAll(findGroupsInDirs(dir + f.getName()));
            } else {
                String fstr = f.getName();
                if (fstr.endsWith(".yml")) {
                    grps.add(dir + fstr.substring(0, fstr.length() - 4));
                }
            }
        }
        return grps;
    }

    public static void replace(Activator newAct) {
        String name = newAct.getName();
        for (Activator a : act)
            if(a.equals(name)) act.remove(a);
        act.add(newAct);
    }

    public static boolean contains(String name) {
        boolean rst = false;
        for (Activator a : act) {
            if (a.equals(name)) return true;
        }
        return rst;
    }

    public static int size() {
        return act.size();
    }

    public static void clear() {
        act.clear();
    }

    public static List<Activator> getActivatorInLocation(World world, int x, int y, int z) {
        return getActivatorInLocation(new Location(world, x, y, z));
    }

    public static List<Activator> getActivatorInLocation(Location loc) {
        List<Activator> found = new ArrayList<>();
        for (Activator a : act)
            if (a.isLocatedAt(loc))
                found.add(a);
        return found;
    }

    public static boolean addActivator(Activator a) {
        if (contains(a.name)) return false;
        act.add(a);
        return true;
    }

    public static void removeActivator(String name) {
        if (act.isEmpty()) return;
        for (int i = act.size() - 1; i >= 0; i--)
            if (act.get(i).equals(name)) act.remove(i);
    }

    public static Activator get(String name) {
        for (Activator a : act)
            if (a.equals(name)) return a;
        return null;
    }

    public static boolean clearFlags(String name) {
        Activator a = get(name);
        if (a == null) return false;
        a.clearFlags();
        return true;
    }

    public static boolean clearActions(String name) {
        Activator a = get(name);
        if (a == null) return false;
        a.clearActions();
        return true;
    }

    public static boolean clearReactions(String name) {
        Activator a = get(name);
        if (a == null) return false;
        a.clearReactions();
        return true;
    }

    public static boolean addFlag(String activator, String flag, String param, boolean not) {
        Activator a = get(activator);
        if (a == null) return false;
        a.addFlag(flag, param, not);
        return true;
    }

    public static boolean addAction(String activator, String action, String param) {
        Activator a = get(activator);
        if (a == null) return false;
        a.addAction(action, param);
        return true;
    }

    public static boolean addReaction(String activator, String action, String param) {
        Activator a = get(activator);
        if (a == null) return false;
        a.addReaction(action, param);
        return true;
    }

    private static void delFiles(String dir) {
        dir = dir + ((dir.isEmpty()) ? "" : File.separator);
        File dirs = new File(plg().getDataFolder() + File.separator + "Activators" + File.separator + dir);
        for (File f : dirs.listFiles())
            if (f.isDirectory()) {
                delFiles(dir + f.getName());
                f.delete();
            } else {
                f.delete();
            }
    }

    public static void saveActivators() {
        delFiles("");
        for (String group : findGroupsFromActivators())
            saveActivators(group);
    }

    public static Set<String> findGroupsFromActivators() {
        Set<String> grps = new HashSet<>();
        for (Activator a : act)
            grps.add(a.getGroup());
        return grps;
    }

    public static String implode(String separator, String... data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length - 1; i++) {
            //data.length - 1 => to not add separator at the end
            if (!data[i].matches(" *")) {//empty string are ""; " "; "  "; and so on
                sb.append(data[i]);
                sb.append(separator);
            }
        }
        sb.append(data[data.length - 1].trim());
        return sb.toString();
    }

    public static void saveActivators(String group) {
        String g = implode(File.separator, group.split("\\/"));

        File f = new File(plg().getDataFolder() + File.separator + "Activators" + File.separator + g + ".yml");
        File dir = new File(f.getPath());
        if (!dir.exists()) dir.mkdirs();

        try {
            if (f.exists()) f.delete();
            f.createNewFile();
        } catch (Exception e) {
            Msg.logMessage("Failed to create configuration to file " + f.getAbsolutePath());
            e.printStackTrace();
            return;
        }
        YamlConfiguration cfg = new YamlConfiguration();
        for (Activator a : act) {
            if (a.group.equalsIgnoreCase(group)) a.saveActivator(cfg);
        }

        try {
            cfg.save(f);
        } catch (Exception e) {
            Msg.logMessage("Failed to save configuration to file " + f.getAbsolutePath());
            e.printStackTrace();
        }
    }

    public static void loadActivators(String group, boolean forced) {
        File f = new File(plg().getDataFolder() + File.separator + "Activators" + File.separatorChar + group + ".yml");
        if (!f.exists()) return;
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(f);
        } catch (Exception e) {
            Msg.logMessage("Failed to load configuration from file " + f.getAbsolutePath());
            e.printStackTrace();
            return;
        }

        if(forced)
            for (String type : cfg.getKeys(false)) {
                if (!ActivatorType.isValid(type)) continue;
                ConfigurationSection cs = cfg.getConfigurationSection(type);
                if (cs == null) continue;
                for (String name : cs.getKeys(false)) {
                    ActivatorType at = ActivatorType.getByName(type);
                    if (at == null) {
                        Msg.logOnce("cannotcreate" + type + name, "Failed to create new activator. Type: " + type + " Name: " + name);
                        continue;
                    }

                    Activator a = createActivator(at, name, group, cfg);
                    if (a == null) {
                        Msg.logOnce("cannotcreate2" + type + name, "Failed to create new activator. Type: " + type + " Name: " + name);
                        continue;
                    }
                    if (!addActivator(a))
                        Msg.logOnce("cannotcreate3", "Failed to create new activator. Type: " + type + " Name: " + name);
                }
            }
        else             for (String type : cfg.getKeys(false)) {
            if (!ActivatorType.isValid(type)) continue;
            ConfigurationSection cs = cfg.getConfigurationSection(type);
            if (cs == null) continue;
            for (String name : cs.getKeys(false)) {
                ActivatorType at = ActivatorType.getByName(type);
                if (at == null) {
                    Msg.logOnce("cannotcreate" + type + name, "Failed to create new activator. Type: " + type + " Name: " + name);
                    continue;
                }

                Activator a = createActivator(at, name, group, cfg);
                if (a == null) {
                    Msg.logOnce("cannotcreate2" + type + name, "Failed to create new activator. Type: " + type + " Name: " + name);
                    continue;
                }
                replace(a);
            }
        }
    }

    private static Activator createActivator(ActivatorType type, String name, String group, YamlConfiguration cfg) {
        try {
            return type.getActivatorClass().getDeclaredConstructor(String.class, String.class, YamlConfiguration.class).newInstance(name, group, cfg);
        } catch (Exception e) {
            Msg.logOnce("cannotcreate" + name, "Failed to create new activator. Name: " + name);
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getActivatorsList() {
        List<String> lst = new ArrayList<>();
        for (Activator a : act)
            if (!act.isEmpty())
                lst.add("&a" + a.toString());
        return lst;
    }

    public static List<String> getActivatorsList(String type) {
        List<String> lst = new ArrayList<>();
        if (!act.isEmpty())
            for (Activator anAct : act) {
                if (type.isEmpty() || anAct.isTypeOf(type)) {
                    lst.add("&a" + anAct.toString());
                }
            }
        return lst;
    }

    public static List<String> getActivatorsListGroup(String group) {
        List<String> lst = new ArrayList<>();
        if (!act.isEmpty())
            for (Activator anAct : act) {
                if (group.isEmpty() || anAct.getGroup().equalsIgnoreCase(group)) {
                    lst.add("&a" + anAct.toString());
                }
            }
        return lst;
    }

    public static boolean activate(RAEvent event) {
        if (act.isEmpty()) return false;
        boolean cancelParentEvent = false;
        for (Activator a : act) {
            if (a.getType().getEventClass().isInstance(event)) {
                if (a.executeActivator(event)) cancelParentEvent = true;
            }
        }
        return cancelParentEvent;
    }

    public static boolean copyAll(String actfrom, String actto) {
        if (!contains(actfrom)) return false;
        if (!contains(actto)) return false;
        copyActions(actfrom, actto);
        copyReactions(actfrom, actto);
        copyFlags(actfrom, actto);
        return true;
    }

    public static boolean copyActions(String actfrom, String actto) {
        if (!contains(actfrom)) return false;
        if (!contains(actto)) return false;
        Activator afrom = get(actfrom);
        Activator ato = get(actto);
        ato.clearActions();
        if (!afrom.getActions().isEmpty()) {
            for (ActVal action : afrom.getActions())
                ato.addAction(action.flag, action.value);
        }
        return true;
    }

    public static boolean copyReactions(String actfrom, String actto) {
        if (!contains(actfrom)) return false;
        if (!contains(actto)) return false;
        Activator afrom = get(actfrom);
        Activator ato = get(actto);
        ato.clearReactions();
        if (!afrom.getReactions().isEmpty()) {
            for (ActVal action : afrom.getReactions())
                ato.addReaction(action.flag, action.value);
        }
        return true;
    }

    public static boolean copyFlags(String actfrom, String actto) {
        if (!contains(actfrom)) return false;
        if (!contains(actto)) return false;
        Activator afrom = get(actfrom);
        Activator ato = get(actto);
        ato.clearFlags();
        if (!afrom.getFlags().isEmpty()) {
            for (FlagVal flag : afrom.getFlags())
                ato.addFlag(flag.flag, flag.value, flag.not);
        }
        return true;
    }

    public static boolean setGroup(String activator, String group) {
        if (!contains(activator)) return false;
        get(activator).setGroup(group);
        return true;
    }

    public static String getGroup(String activator) {
        if (!contains(activator)) return "activator";
        return get(activator).getGroup();
    }

    public static List<ItemHoldActivator> getItemHoldActivatos() {
        List<ItemHoldActivator> ihold = new ArrayList<>();
        for (Activator a : act)
            if (a.getType() == ActivatorType.ITEM_HOLD) ihold.add((ItemHoldActivator) a);
        return ihold;
    }

    public static List<MessageActivator> getMessageActivators() {
        List<MessageActivator> list = new ArrayList<>();
        for (Activator a : act)
            if (a.getType() == ActivatorType.MESSAGE) list.add((MessageActivator) a);
        return list;
    }


    public static List<ItemWearActivator> getItemWearActivatos() {
        List<ItemWearActivator> iwear = new ArrayList<>();
        for (Activator a : act)
            if (a.getType() == ActivatorType.ITEM_WEAR) iwear.add((ItemWearActivator) a);
        return iwear;
    }

    public static List<Activator> getActivators(ActivatorType type) {
        List<Activator> activators = new ArrayList<>();
        for (Activator activator : act)
            if (activator.getType() == type) activators.add(activator);
        return activators;
    }

	/* public static List<TimeIngameActivator> getTimeIngameActivators() {
        List<TimeIngameActivator> timeIngameAct= new ArrayList<TimeIngameActivator>();
        for (Activator a : act)
            if (a.getType() == ActivatorType.TIME_INGAME) timeIngameAct.add((TimeIngameActivator) a);
        return timeIngameAct;
    }

	public static List<TimeServerActivator> getTimeServerActivators() {
		List<TimeServerActivator> timeServerAct= new ArrayList<TimeServerActivator>();
        for (Activator a : act)
            if (a.getType() == ActivatorType.TIME_SERVER) timeServerAct.add((TimeServerActivator) a);
		return timeServerAct;
	} */


    public static boolean stopExec(Player player, String actName) {
        return stopExec(player == null ? "" : player.getName(), actName);
    }

    public static boolean stopExec(String pstr, String actName) {
        stopexec.add(pstr + "#" + actName);
        return true;
    }


    public static boolean isStopped(Player player, String actName, boolean unstop) {
        return isStopped((player == null ? "" : player.getName()), actName, unstop);
    }

    public static boolean isStopped(String pstr, String actName, boolean unstop) {
        String id = pstr + "#" + actName;
        if (!stopexec.contains(id)) return false;
        if (unstop) stopexec.remove(id);
        return true;
    }


}
