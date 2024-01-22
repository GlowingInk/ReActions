package fun.reactions.model.activators;

import fun.reactions.ReActions;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activators.type.ActivatorTypesRegistry;
import fun.reactions.util.ConfigUtils;
import fun.reactions.util.collections.CaseInsensitiveMap;
import fun.reactions.util.collections.CollectionUtils;
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
    private final ReActions.Platform platform;
    private final Plugin plugin;
    private final Logger logger;
    private final File actsFolder;
    private final ActivatorTypesRegistry types;

    private final Map<String, Activator> activatorsNames;
    private final Map<String, Set<Activator>> activatorsGroups;

    private final Search search;

    public ActivatorsManager(@NotNull ReActions.Platform platform) {
        this.plugin = platform.getPlugin();
        this.platform = platform;
        this.types = platform.getActivatorTypes();

        actsFolder = new File(plugin.getDataFolder(), "Activators");
        logger = platform.logger();
        search = new Search();

        activatorsNames = new CaseInsensitiveMap<>();
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
            for (File inner : CollectionUtils.emptyOnNull(file.listFiles())) {
                loadGroupsRecursively(inner, group, clear, true);
            }
        } else if (file.getName().endsWith(".yml")) {
            FileConfiguration cfg = new YamlConfiguration();
            try {
                cfg.load(file);
            } catch (InvalidConfigurationException | IOException ex) {
                logger.warn("Cannot load '" + file.getName() + "' file", ex);
                return;
            }
            String localGroup = file.getName().substring(0, file.getName().length() - 4);
            group = group.isEmpty()
                    ? localGroup
                    : group + File.separator + localGroup;
            if (clear) {
                Set<Activator> activators = activatorsGroups.remove(group);
                if (activators != null) {
                    for (Activator activator : activators) {
                        types.get(activator.getClass()).removeActivator(activator);
                        activatorsNames.remove(activator.getLogic().getName());
                    }
                }
            }
            for (String typeStr : cfg.getKeys(false)) {
                ActivatorType type = types.get(typeStr);
                if (type == null) {
                    logger.warn("Failed to load activators with the unknown type '" + typeStr + "' in the group '"+ group + "'");
                    // TODO Move failed activators to backup
                    continue;
                }
                if (!cfg.isConfigurationSection(typeStr)) {
                    logger.warn("Failed to load activators with the type '" + typeStr + "' - isn't a section");
                    continue;
                }
                ConfigurationSection cfgType = cfg.getConfigurationSection(typeStr);
                //noinspection DataFlowIssue
                for (String name : cfgType.getKeys(false)) {
                    ConfigurationSection cfgActivator = Objects.requireNonNull(cfgType.getConfigurationSection(name));
                    Logic logic = new Logic(platform, type.getName().toUpperCase(Locale.ROOT), name);
                    logic.setGroup(group);
                    logic.load(cfgActivator);
                    Activator activator = type.loadActivator(logic, cfgActivator);
                    if (activator == null || !activator.isValid()) {
                        logger.warn("Failed to load activator '" + name + "' in the group '" + group + "'");
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
            logger.warn("Failed to add activator '" + logic.getName() + "' - activator with this name already exists");
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
        platform.getServer().getScheduler().runTaskAsynchronously(plugin, () -> saveGroup(name, finalActivators));
        return true;
    }

    private void saveGroup(@NotNull String name, @NotNull Set<Activator> activators) {
        File file = new File(actsFolder, name.replace('/', File.separatorChar) + ".yml");
        if (activators.isEmpty()) {
            if (!file.delete()) {
                logger.warn("Failed to delete empty group file '" + name + "'");
            }
            return;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (Activator activator : activators) {
            String typeStr = Objects.requireNonNull(types.get(activator.getClass())).getName();
            ConfigurationSection typeCfg = ConfigUtils.getSection(cfg, typeStr);
            activator.saveActivator(typeCfg.createSection(activator.getLogic().getName()));
        }
        try {
            cfg.save(file);
        } catch (IOException ex) {
            logger.error("Failed to save group '" + name + "'", ex);
        }
    }

    public void activate(@NotNull ActivationContext context, @NotNull String id) {
        Activator activator = activatorsNames.get(id);
        if (activator != null) {
            context.initialize();
            activator.executeActivator(context);
        }
    }

    public boolean activate(@NotNull ActivationContext context) {
        ActivatorType type = types.get(context.getType());
        if (type != null && !type.isEmpty()) {
            context.initialize();
            type.activate(context);
            return true;
        }
        return false;
    }

    public @NotNull Search search() {
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
