package me.fromgate.reactions.logic.activators;

import lombok.Getter;
import me.fromgate.reactions.ReActionsPlugin;
import me.fromgate.reactions.util.RaGenerator;
import me.fromgate.reactions.util.Utils;
import me.fromgate.reactions.util.collections.CaseInsensitiveMap;
import me.fromgate.reactions.util.parameter.Parameters;
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

public class ActivatorsManager {
    private final Plugin plugin;
    private final File actsFolder;
    private final Logger logger;
    @Getter
    private final Query query;

    private final Map<Class<? extends Activator>, ActivatorType> types;
    private final Map<String, ActivatorType> aliasTypes;
    private final Map<String, Activator> activatorByName;
    private final Map<String, Set<Activator>> activatorsByGroup;

    public ActivatorsManager(@NotNull ReActionsPlugin react) {
        plugin = react;
        actsFolder = new File(react.getDataFolder(), "Activators");
        logger = react.getLogger();
        query = new Query();

        types = new HashMap<>();
        aliasTypes = new HashMap<>();
        activatorByName = new CaseInsensitiveMap<>();
        activatorsByGroup = new HashMap<>();
    }

    public void loadGroup(@NotNull String group, boolean clear) {
        actsFolder.mkdirs();
        loadGroupsRecursively(actsFolder, group, clear);
    }

    private void loadGroupsRecursively(@NotNull File file, @NotNull String group, boolean clear) {
        if (!file.exists()) return;
        if (file.getName().endsWith(".yml")) {
            FileConfiguration cfg = new YamlConfiguration();
            try {
                cfg.load(file);
            } catch (InvalidConfigurationException | IOException e) {
                logger.warning("Cannot load '" + file.getName() + "' file!");
                e.printStackTrace();
                return;
            }
            String localGroup = file.getName().substring(0, file.getName().length() - 4);
            group = group.isEmpty() ?
                    localGroup :
                    group + File.separator + localGroup;
            if (clear) {
                Set<Activator> activators = activatorsByGroup.remove(group);
                if (activators != null) for (Activator activator : activators) {
                    types.get(activator.getClass()).removeActivator(activator);
                    activatorByName.remove(activator.getLogic().getName());
                }
            }
            for (String strType : cfg.getKeys(false)) {
                ActivatorType type = getType(strType);
                if (type == null) {
                    logger.warning("Failed to load activators with the unknown type '" + strType + "' in the group '"+ group + "'.");
                    // TODO Move failed activators to backup
                    continue;
                }
                // TODO Replace with some simpler null-safe method
                ConfigurationSection cfgType = Objects.requireNonNull(cfg.getConfigurationSection(strType));
                for (String name : cfgType.getKeys(false)) {
                    ConfigurationSection cfgActivator = Objects.requireNonNull(cfg.getConfigurationSection(name));
                    Activator activator = type.loadActivator(new ActivatorLogic(name, group, cfgActivator), cfgActivator);
                    if (activator == null || !activator.isValid()) {
                        logger.warning("Failed to load activator '" + name + "' in the group '" + group + "'.");
                        continue;
                    }
                    addActivator(activator);
                }
            }
        } else if (file.isDirectory()) {
            group = group.isEmpty() ?
                        file.getName() :
                        group + File.separator + file.getName();
            for (File inner : Objects.requireNonNull(file.listFiles())) {
                loadGroupsRecursively(inner, group, clear);
            }
        }
    }

    public boolean moveActivator(@NotNull Activator activator, @NotNull String newGroup) {
        String oldGroup = activator.getLogic().getGroup();
        if (newGroup.equals(oldGroup)) return false; // TODO Error: moved to same group

        activatorsByGroup.get(oldGroup).remove(activator);
        saveGroup(oldGroup);

        activator.getLogic().setGroup(newGroup);
        activatorsByGroup.computeIfAbsent(newGroup, s -> new HashSet<>()).add(activator);
        saveGroup(newGroup);

        return true;
    }

    public boolean addActivator(@NotNull Activator activator) {
        ActivatorLogic logic = activator.getLogic();
        String name = logic.getName();
        if (activatorByName.containsKey(name)) {
            logger.warning("Failed to add activator '" + logic.getName() + "' - activator with this name already exists!");
            return false;
        }
        types.get(activator.getClass()).addActivator(activator);
        activatorByName.put(logic.getName(), activator);
        activatorsByGroup.computeIfAbsent(logic.getGroup(), g -> new HashSet<>()).add(activator);
        saveGroup(logic.getGroup());
        return true;
    }

    public void clearActivators() {
        types.values().forEach(ActivatorType::clearActivators);
        activatorByName.clear();
        activatorsByGroup.clear();
    }

    public boolean containsActivator(@NotNull String name) {
        return activatorByName.containsKey(name);
    }

    @Nullable
    public Activator removeActivator(@NotNull String name) {
        Activator activator = activatorByName.remove(name);
        if (activator == null) return null;
        activatorsByGroup.get(activator.getLogic().getGroup()).remove(activator);
        types.get(activator.getClass()).removeActivator(activator);
        saveGroup(activator.getLogic().getGroup());
        return activator;
    }

    @Nullable
    public Activator getActivator(@NotNull String name) {
        return activatorByName.get(name);
    }

    public boolean saveGroup(@NotNull String name) {
        Set<Activator> activators = activatorsByGroup.get(name);
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
            logger.warning("Failed to save group '" + name + "'!");
            e.printStackTrace();
            return;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        for (Activator activator : activators) {
            ConfigurationSection typeCfg = cfg.createSection(Objects.requireNonNull(types.get(activator.getClass())).getName());
            activator.getLogic().save(typeCfg.createSection(activator.getLogic().getName()));
        }
        try {
            cfg.save(file);
        } catch (IOException e) {
            logger.warning("Failed to save group '" + name + "'!");
            e.printStackTrace();
        }
    }

    @NotNull
    public List<String> registerType(@NotNull ActivatorType type) {
        if (types.containsKey(type.getType())) {
            throw new IllegalStateException("Activator type '" + type.getType().getSimpleName() + "' is already registered!");
        }
        String name = type.getName().toUpperCase(Locale.ENGLISH);
        if (aliasTypes.containsKey(name)) {
            ActivatorType preserved = aliasTypes.get(name);
            if (preserved.getName().equalsIgnoreCase(name)) {
                throw new IllegalStateException("Activator type name '" + name + "' is already used for '" + preserved.getType().getSimpleName() + "'!");
            } else {
                logger.warning("Activator type name '" + name + "' is already used as an alias for '" + preserved.getType().getSimpleName() + "', overriding it.");
            }
        }
        types.put(type.getType(), type);
        List<String> registeredNames = new ArrayList<>();
        registeredNames.add(name);
        aliasTypes.put(name, type);
        for (String alias : type.getAliases()) {
            aliasTypes.computeIfAbsent(alias.toUpperCase(Locale.ENGLISH), key -> {
                registeredNames.add(key);
                return type;
            });
        }
        return registeredNames;
    }

    @Deprecated
    public void activate(@NotNull Storage storage, @NotNull String id) {
        storage.init();
        activatorByName.get(id).executeActivator(storage);
    }

    public void activate(@NotNull Storage storage) {
        ActivatorType type = types.get(storage.getType());
        if (!type.isEmpty()) {
            storage.init();
            type.activate(storage);
        }
    }

    @Nullable
    public ActivatorType getType(@NotNull String name) {
        return aliasTypes.get(name.toUpperCase(Locale.ENGLISH));
    }

    @Nullable
    public ActivatorType getType(@NotNull Class<? extends Activator> type) {
        return types.get(type);
    }

    @NotNull
    public static ActivatorType typeOf(@NotNull Class<? extends Activator> type, @NotNull String name, @NotNull RaGenerator<Parameters> creator, @NotNull RaGenerator<ConfigurationSection> loader) {
        return typeOf(type, name, creator, loader, false);
    }

    @NotNull
    public static ActivatorType typeOf(@NotNull Class<? extends Activator> type, @NotNull String name, @NotNull RaGenerator<Parameters> creator, @NotNull RaGenerator<ConfigurationSection> loader, boolean needBlock) {
        return new SimpleType(type, name, creator, loader, needBlock);
    }

    private static class SimpleType implements ActivatorType {
        private final Class<? extends Activator> type;
        private final RaGenerator<Parameters> creator;
        private final RaGenerator<ConfigurationSection> loader;
        private final boolean needBlock;
        private final boolean locatable;
        private final String name;
        private final List<String> aliases;
        private final Set<Activator> activators;

        public SimpleType(Class<? extends Activator> type, String name, RaGenerator<Parameters> creator, RaGenerator<ConfigurationSection> loader, boolean needBlock) {
            this.type = type;
            this.creator = creator;
            this.loader = loader;
            this.needBlock = needBlock;
            this.locatable = type.isAssignableFrom(Locatable.class);
            this.name = name;
            this.aliases = List.of(Utils.getAliases(type));
            this.activators = new HashSet<>();
        }

        @NotNull
        @Override
        public Class<? extends Activator> getType() {
            return type;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @NotNull
        @Override
        public List<String> getAliases() {
            return aliases;
        }

        @NotNull
        @Override
        public Set<Activator> getActivators() {
            return activators;
        }

        @Nullable
        @Override
        public Activator createActivator(@NotNull ActivatorLogic logic, @NotNull Parameters params) {
            return creator.generate(logic, params);
        }

        @Nullable
        @Override
        public Activator loadActivator(@NotNull ActivatorLogic logic, @NotNull ConfigurationSection cfg) {
            return loader.generate(logic, cfg);
        }

        @Override
        public boolean isNeedBlock() {
            return needBlock;
        }

        @Override
        public boolean isLocatable() {
            return locatable;
        }

        @Override
        public void addActivator(@NotNull Activator activator) {
            activators.add(activator);
        }

        @Override
        public void removeActivator(@NotNull Activator activator) {
            activators.remove(activator);
        }

        @Override
        public void clearActivators() {
            activators.clear();
        }

        @NotNull
        @Override
        public List<Activator> getActivatorsAt(@NotNull World world, int x, int y, int z) {
            if (!isLocatable()) return Collections.emptyList();
            List<Activator> located = new ArrayList<>();
            for (Activator activator : getActivators()) {
                if (((Locatable) activator).isLocatedAt(world, x, y, z)) located.add(activator);
            }
            return located;
        }

        @Override
        public void activate(@NotNull Storage storage) {
            for (Activator activator : getActivators()) {
                activator.executeActivator(storage);
            }
        }

        @Override
        public boolean isEmpty() {
            return activators.isEmpty();
        }
    }

    public final class Query {
        @NotNull
        public Collection<Activator> all() {
            return activatorByName.values();
        }

        @NotNull
        public Collection<Activator> byGroup(@NotNull String group) {
            return activatorsByGroup.getOrDefault(group, Collections.emptySet());
        }

        @NotNull
        public Collection<Activator> byType(String typeStr) {
            ActivatorType type = getType(typeStr);
            if (type == null) return Collections.emptySet();
            return type.getActivators();
        }

        @NotNull
        public Collection<Activator> byRawLocation(@NotNull World world, int x, int y, int z) {
            List<Activator> found = new ArrayList<>();
            for (ActivatorType type : types.values()) {
                if (type.isLocatable())
                    type.getActivators().stream().filter(act -> ((Locatable) act).isLocatedAt(world, x, y, z)).forEach(found::add);
            }
            return found;
        }

        @NotNull
        public Collection<Activator> byLocation(@NotNull Location location) {
            return byRawLocation(Objects.requireNonNull(location.getWorld()), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        }
    }
}
