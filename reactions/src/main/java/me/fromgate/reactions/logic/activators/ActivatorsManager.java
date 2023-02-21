package me.fromgate.reactions.logic.activators;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.Logic;
import me.fromgate.reactions.logic.activators.type.ActivatorType;
import me.fromgate.reactions.logic.activators.type.ActivatorTypesRegistry;
import me.fromgate.reactions.logic.activity.ActivitiesRegistry;
import me.fromgate.reactions.util.collections.Maps;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class ActivatorsManager {
    private final Plugin plugin;
    private final File actsFolder;
    private final Logger logger;
    private final Search search;
    private final ActivitiesRegistry activity;
    private final ActivatorTypesRegistry types;
    private final Map<String, Activator> activatorsNames;
    private final Map<String, Set<Activator>> activatorsGroups;

    public ActivatorsManager(@NotNull ReActions.Platform react) {
        this.plugin = react.getPlugin();
        this.activity = react.getActivities();
        this.types = react.getActivatorTypes();

        actsFolder = new File(plugin.getDataFolder(), "Activators");
        logger = react.logger();
        search = new Search();

        activatorsNames = Maps.caseInsensitive();
        activatorsGroups = new HashMap<>();
    }

    public void loadGroup(@NotNull String group, boolean clear) {
        actsFolder.mkdirs();
        loadGroupsRecursively(actsFolder, group, clear, false);
    }

    private void loadGroupsRecursively(@NotNull File file, @NotNull String group, boolean clear, boolean useGroup) {
        if (!file.exists()) return;

        if (file.isDirectory()) {
            if (useGroup) {
                group = group.isEmpty()
                        ? file.getName()
                        : group + File.separator + file.getName();
            }
            for (File inner : Objects.requireNonNull(file.listFiles())) {
                loadGroupsRecursively(inner, group, clear, true);
            }
        } else if (file.getName().endsWith(".yml")) {
            FileConfiguration cfg = new YamlConfiguration();
            try {
                cfg.load(file);
            } catch (InvalidConfigurationException | IOException e) {
                logger.warn("Cannot load '" + file.getName() + "' file!");
                e.printStackTrace();
                return;
            }
            String localGroup = file.getName().substring(0, file.getName().length() - 4);
            group = group.isEmpty()
                    ? localGroup
                    : group + File.separator + localGroup;
            if (clear) {
                Set<Activator> activators = activatorsGroups.remove(group);
                if (activators != null) for (Activator activator : activators) {
                    types.get(activator.getClass()).removeActivator(activator);
                    activatorsNames.remove(activator.getLogic().getName());
                }
            }
            for (String strType : cfg.getKeys(false)) {
                ActivatorType type = types.get(strType);
                if (type == null) {
                    logger.warn("Failed to load activators with the unknown type '" + strType + "' in the group '"+ group + "'.");
                    // TODO Move failed activators to backup
                    continue;
                }
                // TODO Replace with some simpler null-safe method
                ConfigurationSection cfgType = Objects.requireNonNull(cfg.getConfigurationSection(strType));
                for (String name : cfgType.getKeys(false)) {
                    ConfigurationSection cfgActivator = Objects.requireNonNull(cfgType.getConfigurationSection(name));
                    Activator activator = type.loadActivator(new Logic(
                            type.getName().toUpperCase(Locale.ROOT),
                            name, group, cfgActivator, activity
                    ), cfgActivator);
                    if (activator == null || !activator.isValid()) {
                        logger.warn("Failed to load activator '" + name + "' in the group '" + group + "'.");
                        continue;
                    }
                    addActivator(activator, false);
                }
            }
        }
    }

    public boolean moveActivator(@NotNull Activator activator, @NotNull String newGroup) {
        String oldGroup = activator.getLogic().getGroup();
        if (newGroup.equals(oldGroup)) return false; // TODO Error: moved to same group

        activatorsGroups.get(oldGroup).remove(activator);
        saveGroup(oldGroup);

        activator.getLogic().setGroup(newGroup);
        activatorsGroups.computeIfAbsent(newGroup, s -> new HashSet<>()).add(activator);
        saveGroup(newGroup);

        return true;
    }

    public boolean addActivator(@NotNull Activator activator, boolean save) {
        Logic logic = activator.getLogic();
        String name = logic.getName();
        if (activatorsNames.containsKey(name)) {
            logger.warn("Failed to add activator '" + logic.getName() + "' - activator with this name already exists!");
            return false;
        }
        Objects.requireNonNull(types.get(activator.getClass())).addActivator(activator);
        activatorsNames.put(logic.getName(), activator);
        activatorsGroups.computeIfAbsent(logic.getGroup(), g -> new HashSet<>()).add(activator);
        if (save) saveGroup(logic.getGroup());
        return true;
    }

    public void clearActivators() {
        types.getTypes().forEach(ActivatorType::clearActivators);
        activatorsNames.clear();
        activatorsGroups.clear();
    }

    public boolean containsActivator(@NotNull String name) {
        return activatorsNames.containsKey(name);
    }

    public @Nullable Activator removeActivator(@NotNull String name) {
        Activator activator = activatorsNames.remove(name);
        if (activator == null) return null;
        activatorsGroups.get(activator.getLogic().getGroup()).remove(activator);
        types.get(activator.getClass()).removeActivator(activator);
        saveGroup(activator.getLogic().getGroup());
        return activator;
    }

    public @Nullable Activator getActivator(@NotNull String name) {
        return activatorsNames.get(name);
    }

    public @NotNull @UnmodifiableView Collection<String> getActivatorNames() {
        return Collections.unmodifiableCollection(activatorsNames.keySet());
    }

    public boolean saveGroup(@NotNull String name) {
        Set<Activator> activators = activatorsGroups.get(name);
        if (activators == null) return false;
        Set<Activator> finalActivators = new HashSet<>(activators);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> saveGroup(name, finalActivators));
        return true;
    }

    private void saveGroup(@NotNull String name, @NotNull Set<Activator> activators) {
        File file = new File(actsFolder, name.replace('/', File.separatorChar) + ".yml");
        if (activators.isEmpty()) {
            file.delete();
            return;
        }
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        } else {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            logger.warn("Failed to save group '" + name + "'!");
            e.printStackTrace();
            return;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (Activator activator : activators) {
            String type = Objects.requireNonNull(types.get(activator.getClass())).getName();
            ConfigurationSection typeCfg = cfg.isConfigurationSection(type)
                    ? Objects.requireNonNull(cfg.getConfigurationSection(type))
                    : cfg.createSection(type);
            activator.saveActivator(typeCfg.createSection(activator.getLogic().getName()));
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            logger.warn("Failed to save group '" + name + "'!");
            e.printStackTrace();
        }
    }

    public void activate(@NotNull ActivationContext details, @NotNull String id) {
        details.initialize();
        activatorsNames.get(id).executeActivator(details);
    }

    public boolean activate(@NotNull ActivationContext details) {
        ActivatorType type = types.get(details.getType());
        if (type != null && !type.isEmpty()) {
            details.initialize();
            type.activate(details);
            return true;
        }
        return false;
    }

    public Search search() {
        return search;
    }

    public final class Search {
        @NotNull
        public Collection<Activator> all() {
            return Collections.unmodifiableCollection(activatorsNames.values());
        }

        @NotNull
        public Collection<Activator> byGroup(@NotNull String group) {
            return Collections.unmodifiableCollection(activatorsGroups.getOrDefault(group, Set.of()));
        }

        @NotNull
        public Collection<Activator> byType(String typeStr) {
            ActivatorType type = types.get(typeStr);
            if (type == null) return Set.of();
            return Collections.unmodifiableCollection(type.getActivators());
        }

        @NotNull
        public Collection<Activator> byRawLocation(@NotNull World world, int x, int y, int z) {
            List<Activator> found = new ArrayList<>();
            for (ActivatorType type : types.getTypes()) {
                if (Locatable.class.isAssignableFrom(type.getActivatorClass())) {
                    type.getActivators().stream().filter(act -> ((Locatable) act).isLocatedAt(world, x, y, z)).forEach(found::add);
                }
            }
            return Collections.unmodifiableCollection(found);
        }

        @NotNull
        public Collection<Activator> byLocation(@NotNull Location location) {
            return byRawLocation(Objects.requireNonNull(location.getWorld()), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }

        @NotNull
        public Collection<Activator> query(@NotNull Predicate<Activator> predicate) {
            List<Activator> found = new ArrayList<>();
            for (Activator activator : activatorsNames.values()) {
                if (predicate.test(activator)) found.add(activator);
            }
            return found;
        }
    }
}
