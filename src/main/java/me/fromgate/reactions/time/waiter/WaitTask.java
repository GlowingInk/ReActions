package me.fromgate.reactions.time.waiter;

import lombok.Getter;
import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.actions.StoredAction;
import me.fromgate.reactions.util.TimeUtils;
import me.fromgate.reactions.util.data.RaContext;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WaitTask implements Runnable {
    @Getter
    private final String taskId;
    @Getter
    private String playerName;
    @Getter
    private boolean executed;
    private List<StoredAction> actions;
    private long executionTime;
    private BukkitTask task;

    public WaitTask(String playerName, List<StoredAction> actions, long time) {
        this.taskId = UUID.randomUUID().toString();
        this.playerName = playerName;
        this.actions = actions;
        this.executed = false;
        this.executionTime = System.currentTimeMillis() + time;
        task = Bukkit.getScheduler().runTaskLater(ReActions.getPlugin(), this, TimeUtils.timeToTicks(time));
    }

    public WaitTask(YamlConfiguration cfg, String taskId) {
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
        if (System.currentTimeMillis() > executionTime + WaitingManager.getTimeLimit()) this.executed = true;
        if (p == null && playerName != null) return;
        Bukkit.getScheduler().runTask(ReActions.getPlugin(), () -> actions.forEach(action -> action.getAction().execute(RaContext.EMPTY_CONTEXT, action.getParameters())));
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

}
