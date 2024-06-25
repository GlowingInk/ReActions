package fun.reactions.time.wait;

import fun.reactions.ReActions;
import fun.reactions.model.activity.ActivitiesRegistry;
import fun.reactions.model.activity.actions.Action;
import fun.reactions.model.environment.Variables;
import fun.reactions.save.Saveable;
import fun.reactions.util.ConfigUtils;
import fun.reactions.util.time.TimeUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static fun.reactions.util.time.TimeUtils.offsetUntil;
import static fun.reactions.util.time.TimeUtils.timeToTicks;

public class WaitingManager implements Saveable {
    private final ReActions.Platform rea;

    private static AttachedBehaviour behaviour = AttachedBehaviour.SKIP;
    private static long timeLimit;

    private final SortedSet<WaitTask> tasks;
    private final Set<WaitTask> toSchedule;
    private boolean requiresUpdate; // We don't want ConcurrentHashMap to do extra work

    private boolean init;

    private BukkitTask executor;
    private WaitTask next;

    public WaitingManager(@NotNull ReActions.Platform rea) {
        this.rea = rea;
        this.tasks = new TreeSet<>();
        this.toSchedule = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public static void setBehaviour(@NotNull AttachedBehaviour behaviour) {
        WaitingManager.behaviour = behaviour;
    }

    public static void setHoursLimit(long hours) {
        WaitingManager.timeLimit = hours * TimeUtils.MS_PER_HOUR;
    }

    @ApiStatus.Internal
    public void init() {
        if (init) {
            throw new IllegalStateException("WaitingManager is already initialized");
        }
        init = true;
        load();
        rea.getServer().getScheduler().runTaskTimer(rea.getPlugin(), this::updateTasks, 1L, 1L);
    }

    private void updateTasks() {
        if (requiresUpdate) {
            requiresUpdate = false;
            for (var iterator = toSchedule.iterator(); iterator.hasNext();) {
                tasks.add(iterator.next());
                iterator.remove();
            }
        }

        if (tasks.isEmpty()) return;
        WaitTask first = tasks.first();
        if (next != first) { // Nearest task is updated, rescheduling
            next = first;
            if (executor != null && !executor.isCancelled()) {
                executor.cancel();
                executor = null;
            }
            long offsetTicks = timeToTicks(offsetUntil(next.executionTime()));
            if (offsetTicks < 1) {
                runTasks();
            } else {
                executor = rea.getServer().getScheduler().runTaskLater(
                        rea.getPlugin(),
                        this::runTasks,
                        offsetTicks
                );
            }
        } else if (first.isTime()) {
            runTasks();
        }
    }

    private void runTasks() {
        var iterator = tasks.iterator();
        while (iterator.hasNext()) {
            WaitTask task = iterator.next();
            if (!task.isTime()) {
                break;
            }
            if (task.playerId() != null && rea.getServer().getPlayer(task.playerId()) == null) {
                switch (behaviour) {
                    case SKIP -> {
                        if (timeLimit > TimeUtils.offsetFrom(task.executionTime())) {
                            iterator.remove();
                        }
                        continue;
                    }
                    case DISCARD -> {
                        iterator.remove();
                        continue;
                    }
                    case EXECUTE -> {}
                }
            }
            task.execute(rea);
            iterator.remove();
        }
        this.next = tasks.isEmpty() ? null : tasks.first();
    }

    public void schedule(@NotNull WaitTask task) {
        if (task.actions().isEmpty()) return;
        toSchedule.add(task);
        requiresUpdate = true;
    }

    public enum AttachedBehaviour {
        SKIP, EXECUTE, DISCARD;

        public static @Nullable AttachedBehaviour getByName(@Nullable String name) {
            if (name == null) return null;
            return switch (name.toUpperCase(Locale.ROOT)) {
                case "SKIP" -> SKIP;
                case "EXECUTE" -> EXECUTE;
                case "DISCARD" -> DISCARD;
                default -> null;
            };
        }
    }

    private void load() {
        YamlConfiguration cfg = new YamlConfiguration();
        if (!ConfigUtils.loadConfig(cfg, new File(rea.getDataFolder(), "delayed-actions.yml"), "Failed to load delayed actions")) return;
        ActivitiesRegistry activities = rea.getActivities();
        for (String key : cfg.getKeys(false)) {
            var taskCfg = Objects.requireNonNull(cfg.getConfigurationSection(key));
            List<String> actionsCfg = taskCfg.isList("actions")
                    ? taskCfg.getStringList("actions")
                    : taskCfg.getStringList("actions.list"); // Legacy
            List<Action.Stored> actions = new ArrayList<>(actionsCfg.size());
            for (String str : actionsCfg) {
                actions.add(activities.storedActionOf(str));
            }
            UUID playerId;
            if (taskCfg.isString("player-id")) {
                try {
                    playerId = UUID.fromString(taskCfg.getString("player-id", ""));
                } catch (Exception ignored) {
                    playerId = null;
                }
            } else if (taskCfg.isString("player")) { // Legacy
                OfflinePlayer offPlayer = rea.getServer().getOfflinePlayerIfCached(taskCfg.getString("player", ""));
                playerId = offPlayer == null ? null : offPlayer.getUniqueId();
            } else {
                playerId = null;
            }
            Variables vars;
            if (taskCfg.isConfigurationSection("variables")) {
                //noinspection ConstantConditions
                vars = Variables.readConfiguration(taskCfg.getConfigurationSection("variables"));
            } else {
                vars = new Variables();
            }
            long executionTime = taskCfg.getLong("execution-time");
            tasks.add(new WaitTask(
                    vars,
                    playerId,
                    actions,
                    executionTime
            ));
        }
    }

    @Override
    public void save() {
        save(true);
    }

    @Override
    public void saveSync() {
        save(false);
    }

    private void save(boolean async) {
        YamlConfiguration cfg = new YamlConfiguration();
        for (WaitTask task : tasks) {
            var taskCfg = cfg.createSection(Long.toString(task.executionTime() * System.identityHashCode(task)));
            if (task.playerId() != null) taskCfg.set("player-id", task.playerId().toString());
            taskCfg.set("execution-time", task.executionTime());
            List<String> actions = new ArrayList<>(task.actions().size());
            for (var action : task.actions()) {
                actions.add(action.toString());
            }
            taskCfg.set("actions", actions);
            Variables vars = task.variables();
            if (!vars.isEmpty()) {
                var varsCfg = taskCfg.createSection("variables");
                for (String varKey : vars.keys()) {
                    varsCfg.set(varKey, vars.getString(varKey));
                }
            }
        }
        Runnable saveRun = () -> ConfigUtils.saveConfig(
                cfg,
                new File(rea.getDataFolder(), "delayed-actions.yml"),
                "Failed to save delayed actions"
        );
        if (async) {
            rea.getServer().getScheduler().runTaskAsynchronously(rea.getPlugin(), saveRun);
        } else {
            saveRun.run();
        }
    }
}
