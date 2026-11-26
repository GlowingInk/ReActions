package fun.reactions.model.activators;

import fun.reactions.ReActions;
import fun.reactions.model.Logic;
import fun.reactions.model.activators.type.ActivatorType;
import fun.reactions.model.activators.type.ActivatorTypesRegistry;
import fun.reactions.util.ConfigUtils;
import fun.reactions.util.collections.CollectionUtils;
import ink.glowing.utils.hash.CaseInsensitive;
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

    private final Map<String, Activator> activatorsByNames;
    private final Map<String, Set<Activator>> activatorsByGroups;
    private final Map<Class<? extends Activator>, Set<Activator>> activatorsByTypes;

    private final Search search;

    public ActivatorsManager(@NotNull ReActions.Platform platform) {
        this.plugin = platform.getPlugin();
        this.platform = platform;
        this.types = platform.getActivatorTypes();

        actsFolder = new File(plugin.getDataFolder(), "Activators");
        logger = platform.logger();
        search = new Search();

        activatorsByNames = CaseInsensitive.newLinkedMap();
        activatorsByGroups = new HashMap<>();
        activatorsByTypes = new HashMap<>();
    }

    public int loadGroup(@NotNull String group, boolean clear) {
        actsFolder.mkdirs();
        if (clear) clearGroup(group);
        int[] loaded = {0};
        if (group.isEmpty()) {
            loadGroupsRecursively(actsFolder, "", false, loaded);
        } else {
            int split = group.lastIndexOf(File.separatorChar);
            String parent = split == -1 ? "" : group.substring(0, split);
            loadGroupsRecursively(new File(actsFolder, group), parent, true, loaded);
            loadGroupsRecursively(new File(actsFolder, group + ".yml"), parent, true, loaded);
        }
        return loaded[0];
    }

    private void clearGroup(@NotNull String group) {
        if (group.isEmpty()) {
            clearActivators();
            return;
        }
        String subgroupPrefix = group + File.separator;
        Iterator<Map.Entry<String, Set<Activator>>> iterator = activatorsByGroups.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Set<Activator>> entry = iterator.next();
            if (!entry.getKey().equals(group) && !entry.getKey().startsWith(subgroupPrefix)) continue;
            for (Activator activator : entry.getValue()) {
                detachFromType(activator);
                activatorsByNames.remove(activator.getLogic().getName());
            }
            iterator.remove();
        }
    }

    private void loadGroupsRecursively(
            @NotNull File file,
            @NotNull String group,
            boolean useGroup,
            int @NotNull [] loaded
    ) {
        if (!file.exists()) return;

        if (file.isDirectory()) {
            if (useGroup) {
                group = group.isEmpty()
                        ? file.getName()
                        : group + File.separator + file.getName();
            }
            for (File inner : CollectionUtils.emptyOnNull(file.listFiles())) {
                loadGroupsRecursively(inner, group, true, loaded);
            }
        } else if (file.getName().endsWith(".yml")) {
            FileConfiguration cfg = new YamlConfiguration();
            try {
                cfg.load(file);
            } catch (InvalidConfigurationException | IOException ex) {
                logger.warn("Cannot load '{}' file", file.getName(), ex);
                return;
            }
            String localGroup = file.getName().substring(0, file.getName().length() - 4);
            group = group.isEmpty()
                    ? localGroup
                    : group + File.separator + localGroup;
            for (String typeStr : cfg.getKeys(false)) {
                ActivatorType type = types.get(typeStr);
                if (type == null) {
                    logger.warn("Failed to load activators with the unknown type '{}' in the group '{}'", typeStr, group);
                    // TODO Move failed activators to backup
                    continue;
                }
                if (!cfg.isConfigurationSection(typeStr)) {
                    logger.warn("Failed to load activators with the type '{}' - isn't a section", typeStr);
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
                        logger.warn("Failed to load activator '{}' in the group '{}'", name, group);
                        continue;
                    }
                    addActivator(activator, false);
                    loaded[0]++;
                }
            }
        }
    }

    public boolean moveActivator(@NotNull Activator activator, @NotNull String newGroup) {
        if (newGroup.equals(activator.getLogic().getGroup())) return false;
        detachFromGroup(activator);
        activator.getLogic().setGroup(newGroup);
        activatorsByGroups.computeIfAbsent(newGroup, _ -> new HashSet<>()).add(activator);
        saveGroup(newGroup);
        return true;
    }

    private void detachFromGroup(@NotNull Activator activator) {
        String group = activator.getLogic().getGroup();
        Set<Activator> remaining = activatorsByGroups.get(group);
        remaining.remove(activator);
        if (remaining.isEmpty()) activatorsByGroups.remove(group);
        Set<Activator> snapshot = new HashSet<>(remaining);
        platform.getServer().getScheduler().runTaskAsynchronously(plugin, () -> saveGroup(group, snapshot, activator));
    }

    public boolean addActivator(@NotNull Activator activator, boolean save) {
        Logic logic = activator.getLogic();
        String name = logic.getName();
        if (activatorsByNames.containsKey(name)) {
            logger.warn("Failed to add activator '{}' - activator with this name already exists", name);
            return false;
        }
        activatorsByTypes.computeIfAbsent(activator.getClass(), _ -> new HashSet<>()).add(activator);
        activatorsByNames.put(name, activator);
        activatorsByGroups.computeIfAbsent(logic.getGroup(), _ -> new HashSet<>()).add(activator);
        typeOf(activator).include(activator);
        if (save) saveGroup(logic.getGroup());
        return true;
    }

    private void detachFromType(@NotNull Activator activator) {
        Set<Activator> remaining = activatorsByTypes.get(activator.getClass());
        if (remaining != null) {
            remaining.remove(activator);
            if (remaining.isEmpty()) activatorsByTypes.remove(activator.getClass());
        }
        typeOf(activator).discard(activator);
    }

    private @NotNull ActivatorType typeOf(@NotNull Activator activator) {
        return Objects.requireNonNull(types.get(activator.getClass()));
    }

    public void clearActivators() {
        for (Activator activator : activatorsByNames.values()) {
            typeOf(activator).discard(activator);
        }
        activatorsByTypes.clear();
        activatorsByNames.clear();
        activatorsByGroups.clear();
    }

    public boolean containsActivator(@NotNull String name) {
        return activatorsByNames.containsKey(name);
    }

    public @Nullable Activator removeActivator(@NotNull String name) {
        Activator activator = activatorsByNames.remove(name);
        if (activator == null) return null;
        detachFromType(activator);
        detachFromGroup(activator);
        return activator;
    }

    public @Nullable Activator getActivator(@NotNull String name) {
        return activatorsByNames.get(name);
    }

    public @NotNull @UnmodifiableView Collection<String> getActivatorNames() {
        return Collections.unmodifiableCollection(activatorsByNames.keySet());
    }

    public @NotNull @UnmodifiableView Collection<String> getGroupNames() {
        return Collections.unmodifiableCollection(activatorsByGroups.keySet());
    }

    public boolean saveGroup(@NotNull String name) {
        Set<Activator> activators = activatorsByGroups.get(name);
        if (activators == null) return false;
        Set<Activator> finalActivators = new HashSet<>(activators);
        platform.getServer().getScheduler().runTaskAsynchronously(plugin, () -> saveGroup(name, finalActivators, null));
        return true;
    }

    private void saveGroup(@NotNull String name, @NotNull Set<Activator> activators, @Nullable Activator removed) {
        File file = new File(actsFolder, name.replace('/', File.separatorChar) + ".yml");
        if (activators.isEmpty()) {
            if (!file.delete()) {
                logger.warn("Failed to delete empty group file '{}'", name);
            }
            return;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        if (removed != null) {
            ConfigurationSection typeCfg = cfg.getConfigurationSection(typeOf(removed).getName());
            if (typeCfg != null) typeCfg.set(removed.getLogic().getName(), null);
        }
        for (Activator activator : activators) {
            try {
                String typeStr = typeOf(activator).getName();
                ConfigurationSection typeCfg = ConfigUtils.getSection(cfg, typeStr);
                activator.saveActivator(typeCfg.createSection(activator.getLogic().getName()));
            } catch (Exception ex) {
                logger.error("Something went wrong during activator saving. {}/{} ({})", activator.getLogic().getGroup(), activator.getLogic().getName(), activator.getLogic().getType(), ex);
            }
        }
        try {
            cfg.save(file);
        } catch (IOException ex) {
            logger.error("Failed to save group '{}'", name, ex);
        }
    }

    public void activate(@NotNull ActivationContext context, @NotNull String id) {
        Activator activator = activatorsByNames.get(id);
        if (activator != null) {
            context.initialize();
            activator.executeActivator(context);
        }
    }

    public static void executeLogic(@NotNull ActivationContext context, @NotNull Collection<? extends Activator> activators) {
        if (activators.isEmpty()) return;
        context.initialize();
        for (Activator activator : activators) {
            activator.executeLogic(context);
        }
    }

    public boolean activate(@NotNull ActivationContext context) {
        Set<Activator> activators = activatorsByTypes.get(context.getType());
        if (activators == null) return false;
        context.initialize();
        for (Activator activator : activators) {
            activator.executeActivator(context);
        }
        return true;
    }

    public @NotNull Search search() {
        return search;
    }

    public final class Search {
        @NotNull
        public Collection<Activator> all() {
            return Collections.unmodifiableCollection(activatorsByNames.values());
        }

        @NotNull
        public Collection<Activator> byGroup(@NotNull String group) {
            return Collections.unmodifiableCollection(activatorsByGroups.getOrDefault(group, Set.of()));
        }

        @NotNull
        public Collection<Activator> byType(String typeStr) {
            ActivatorType type = types.get(typeStr);
            if (type == null) return Set.of();
            return Collections.unmodifiableCollection(activatorsByTypes.getOrDefault(type.getActivatorClass(), Set.of()));
        }

        @NotNull
        @SuppressWarnings("unchecked")
        public <T extends Activator> Collection<T> byType(@NotNull Class<T> type) {
            return Collections.unmodifiableCollection((Set<T>) activatorsByTypes.getOrDefault(type, Set.of()));
        }

        @NotNull
        public Collection<Activator> byRawLocation(@NotNull World world, int x, int y, int z) {
            List<Activator> found = new ArrayList<>();
            for (var entry : activatorsByTypes.entrySet()) {
                if (!Locatable.class.isAssignableFrom(entry.getKey())) continue;
                for (Activator activator : entry.getValue()) {
                    if (((Locatable) activator).isLocatedAt(world, x, y, z)) found.add(activator);
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
            for (Activator activator : activatorsByNames.values()) {
                if (predicate.test(activator)) found.add(activator);
            }
            return found;
        }
    }
}
