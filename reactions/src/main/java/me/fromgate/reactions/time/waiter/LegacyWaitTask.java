package me.fromgate.reactions.time.waiter;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.logic.context.Environment;
import me.fromgate.reactions.util.TimeUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Deprecated
public class LegacyWaitTask implements Runnable {
    private final String taskId;
    private String playerName;
    private boolean executed;
    private List<StoredAction> actions;
    private long executionTime;
    private BukkitTask task;

    public LegacyWaitTask(String playerName, List<StoredAction> actions, long time) {
        this.taskId = UUID.randomUUID().toString();
        this.playerName = playerName;
        this.actions = actions;
        this.executed = false;
        this.executionTime = System.currentTimeMillis() + time;
        task = Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), this, TimeUtils.timeToTicks(time));
    }

    public LegacyWaitTask(YamlConfiguration cfg, String taskId) {
        this.taskId = taskId;
        this.load(cfg, taskId);
        long time = this.executionTime - System.currentTimeMillis();
        if (time < 0) this.execute();
        else
            task = Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), this, TimeUtils.timeToTicks(time));
    }

    @Override
    public void run() {
        execute();
    }

    public void execute() {
        if (this.isExecuted()) return;
        Player p = playerName == null ? null : Bukkit.getPlayerExact(playerName);
        if (System.currentTimeMillis() > executionTime + LegacyWaitingManager.getTimeLimit()) this.executed = true;
        if (p == null && playerName != null) return;
        Bukkit.getScheduler().runTask(ReActions.getPlugin(), () -> actions.forEach(action -> action.getAction().proceed(Environment.anonymous(), action.getParameters())));
        this.executed = true;
    }

    public void stop() {
        this.task.cancel();
        this.task = null;
    }

    public boolean isTimePassed() {
        return this.executionTime < System.currentTimeMillis();
    }

    public void save(YamlConfiguration cfg) {
        cfg.set(this.taskId + ".player", this.playerName == null ? "" : this.playerName);
        cfg.set(this.taskId + ".execution-time", this.executionTime);
        List<String> actionList = new ArrayList<>();
        for (StoredAction a : this.actions) {
            actionList.add(a.toString());
        }
        cfg.set(this.taskId + ".actions.list", actionList);
    }

    public void load(YamlConfiguration cfg, String root) {
        this.playerName = cfg.getString(root + ".player");
        this.executionTime = cfg.getLong(root + ".execution-time", 0);
        List<String> actionList = cfg.getStringList(root + ".actions.list");
        this.actions = new ArrayList<>();
        for (String a : actionList) {
            if (a.contains("=")) {
                String[] split = a.split("=", 2);
                if (split.length < 2) continue;
                Action action = ReActions.getActivities().getAction(split[0]);
                if (action == null) continue;
                this.actions.add(new StoredAction(action, split[1]));
            }
        }
    }

    public String getTaskId() {return this.taskId;}

    public String getPlayerName() {return this.playerName;}

    public boolean isExecuted() {return this.executed;}
}
