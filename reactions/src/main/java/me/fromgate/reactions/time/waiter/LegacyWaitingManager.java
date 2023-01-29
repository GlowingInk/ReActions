package me.fromgate.reactions.time.waiter;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.util.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// TODO: Recode from scratch
@Deprecated
public final class LegacyWaitingManager {

    private static Set<LegacyWaitTask> tasks;
    private static long timeLimit;

    private LegacyWaitingManager() {throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");}

    public static void init() {
        tasks = Collections.newSetFromMap(new ConcurrentHashMap<>()); //new HashSet<>();
        load();
    }

    public static void schedule(Player player, StoredAction action, long time) {
        if (action == null) return;
        schedule(player, List.of(action), time);
    }

    public static void schedule(Player player, List<StoredAction> actions, long time) {
        if (actions.isEmpty()) return;
        String playerStr = player != null ? player.getName() : null;
        LegacyWaitTask task = new LegacyWaitTask(playerStr, actions, time);
        tasks.add(task);
        save();
    }

    public static void remove(LegacyWaitTask task) {
        tasks.remove(task);
        save();
    }

    public static void load() {
        if (!tasks.isEmpty()) {
            for (LegacyWaitTask t : tasks) {
                t.stop();
            }
        }
        tasks.clear();
        YamlConfiguration cfg = new YamlConfiguration();
        File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "delayed-actions.yml");
        if (!FileUtils.loadCfg(cfg, f, "Failed to load delayed actions")) return;
        for (String key : cfg.getKeys(false)) {
            LegacyWaitTask t = new LegacyWaitTask(cfg, key);
            tasks.add(t);
        }
        Bukkit.getScheduler().runTaskTimerAsynchronously(ReActions.getPlugin(), LegacyWaitingManager::refresh, 1, 1);
    }

    public static void refreshPlayer(Player player) {
        refreshPlayer(player.getName());
    }

    public static void refreshPlayer(String player) {
        if (tasks.isEmpty()) return;
        int before = tasks.size();
        Iterator<LegacyWaitTask> iter = tasks.iterator();
        while (iter.hasNext()) {
            LegacyWaitTask t = iter.next();
            if (player.equals(t.getPlayerName()) && t.isTimePassed()) t.execute();
            if (t.isExecuted()) iter.remove();
        }
        if (tasks.size() != before) save();
    }

    public static void refresh() {
        if (tasks.isEmpty()) return;
        int before = tasks.size();
        Iterator<LegacyWaitTask> iter = tasks.iterator();
        while (iter.hasNext()) {
            LegacyWaitTask t = iter.next();
            if (t.isTimePassed()) t.execute();
            if (t.isExecuted()) iter.remove();
        }
        if (tasks.size() != before) save();
    }

    public static void save() {
        Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), () -> {
            YamlConfiguration cfg = new YamlConfiguration();
            File f = new File(ReActions.getPlugin().getDataFolder() + File.separator + "delayed-actions.yml");
            if (f.exists()) f.delete();
            for (LegacyWaitTask t : tasks) {
                if (!t.isExecuted()) t.save(cfg);
            }
            FileUtils.saveCfg(cfg, f, "Failed to save delayed actions");
        }, 1);
    }

    public static long getTimeLimit() {
        return timeLimit;
    }

    public static void setTimeLimit(long ms) {
        timeLimit = ms;
    }
}