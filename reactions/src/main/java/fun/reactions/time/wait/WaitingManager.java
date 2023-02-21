package fun.reactions.time.wait;

import fun.reactions.ReActions;
import fun.reactions.logic.activity.ActivitiesRegistry;
import fun.reactions.logic.activity.actions.StoredAction;
import fun.reactions.logic.environment.Variables;
import fun.reactions.save.Saveable;
import fun.reactions.util.FileUtils;
import fun.reactions.util.TimeUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
        rea.getServer().getScheduler().runTaskTimer(rea.getPlugin(), this::updateTasks, 1L, 1L);
        load();
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
            long offset = TimeUtils.offsetUntil(next.executionTime());
            if (offset < 1) {
                runTasks();
            } else {
                executor = rea.getServer().getScheduler().runTaskLater(
                        rea.getPlugin(),
                        this::runTasks,
                        TimeUtils.timeToTicks(offset) + 1
                );
            }
        }
    }

    private void runTasks() {
        var iterator = tasks.iterator();
        while (iterator.hasNext()) {
            WaitTask task = iterator.next();
            if (!task.isTime()) break;
            if (task.playerId() != null && rea.getServer().getPlayer(task.playerId()) == null) switch (behaviour) {
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
        if (!FileUtils.loadCfg(cfg, new File(rea.getDataFolder(), "delayed-actions.yml"), "Failed to load delayed actions")) return;
        ActivitiesRegistry activities = rea.getActivities();
        for (String key : cfg.getKeys(false)) {
            var taskCfg = Objects.requireNonNull(cfg.getConfigurationSection(key));
            List<String> actionsCfg = taskCfg.isList("actions")
                    ? taskCfg.getStringList("actions")
                    : taskCfg.getStringList("actions.list"); // Legacy
            List<StoredAction> actions = new ArrayList<>(actionsCfg.size());
            for (String str : actionsCfg) {
                StoredAction action = activities.storedActionOf(str);
                if (action != null) actions.add(action);
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
        BukkitScheduler scheduler = rea.getServer().getScheduler();
        scheduler.runTaskAsynchronously(rea.getPlugin(), () -> {
            File file = new File(rea.getDataFolder(), "delayed-actions.yml");
            if (file.exists()) file.delete();
            YamlConfiguration cfg = new YamlConfiguration();
            if (!FileUtils.loadCfg(cfg, file, "Failed to load delayed actions")) return;
            scheduler.runTask(rea.getPlugin(), () -> {
                for (WaitTask task : tasks) {
                    UUID id = UUID.randomUUID();
                    var taskCfg = cfg.createSection(id.toString());
                    if (task.playerId() != null) taskCfg.set("player-id", task.playerId().toString());
                    taskCfg.set("execution-time", task.executionTime());
                    taskCfg.set("actions", task.actions().stream().map(StoredAction::toString).collect(Collectors.toList()));
                    Variables vars = task.variables();
                    if (!vars.isEmpty()) {
                        var varsCfg = taskCfg.createSection("variables");
                        for (String varKey : vars.keys()) {
                            varsCfg.set(varKey, vars.getString(varKey));
                        }
                    }
                }
                scheduler.runTaskAsynchronously(
                        rea.getPlugin(),
                        () -> FileUtils.saveCfg(cfg, file, "Failed to save delayed actions")
                );
            });
        });
    }

    @Override
    public void saveSync() {
        File file = new File(rea.getDataFolder(), "delayed-actions.yml");
        if (file.exists()) file.delete();
        YamlConfiguration cfg = new YamlConfiguration();
        if (!FileUtils.loadCfg(cfg, file, "Failed to load delayed actions")) return;
        for (WaitTask task : tasks) {
            UUID id = UUID.randomUUID();
            ConfigurationSection taskCfg = cfg.createSection(id.toString());
            if (task.playerId() != null) taskCfg.set("player-id", task.playerId().toString());
            taskCfg.set("execution-time", task.executionTime());
            taskCfg.set("actions", task.actions().stream().map(StoredAction::toString).collect(Collectors.toList()));
            Variables vars = task.variables();
            if (!vars.isEmpty()) {
                var varsCfg = taskCfg.createSection("variables");
                for (String varKey : vars.keys()) {
                    varsCfg.set(varKey, vars.getString(varKey));
                }
            }
        }
        FileUtils.saveCfg(cfg, file, "Failed to save delayed actions");
    }
}
