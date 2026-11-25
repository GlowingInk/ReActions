package fun.reactions.module;

import fun.reactions.ReActions;
import fun.reactions.cfg.Reloadable;
import fun.reactions.util.collections.CollectionUtils;
import fun.reactions.util.naming.Named;
import ink.glowing.utils.hash.CaseInsensitive;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;

public class ModulesRegistry {
    private static final String SERVICES_PATH = "META-INF/services/" + Module.class.getName();

    private final ReActions.Platform platform;
    private final File modulesFolder;
    private final Map<String, Module> loadedModules;
    private final List<URLClassLoader> classLoaders;
    private final List<Listener> pendingListeners;
    private Map<String, Module> later;
    private boolean loaded;
    private boolean enabled;

    public ModulesRegistry(@NotNull ReActions.Platform platform) {
        this.platform = platform;
        this.modulesFolder = new File(platform.getDataFolder(), "Modules");
        this.later = CaseInsensitive.newLinkedMap();
        this.loadedModules = CaseInsensitive.newLinkedMap();
        this.classLoaders = new ArrayList<>();
        this.pendingListeners = new ArrayList<>();
    }

    public @Nullable Module getModule(@NotNull String name) {
        return loadedModules.get(name);
    }

    @ApiStatus.Internal
    public void registerPending() {
        if (later == null) {
            throw new IllegalStateException("Pending modules are already registered.");
        }
        Map<String, Module> pending = later;
        later = null;
        if (pending.isEmpty()) return;
        debugInfo("Registering pending modules.");
        Set<String> failed = CaseInsensitive.newSet();
        for (Module module : pending.values()) {
            resolve(module, pending, new ArrayList<>(), failed);
        }
    }

    private boolean resolve(
            @NotNull Module module,
            @NotNull Map<String, Module> pending,
            @NotNull List<Module> path,
            @NotNull Set<String> failed
    ) {
        if (loadedModules.containsKey(module.getName())) return true;
        if (failed.contains(module.getName())) return false;
        path.add(module);
        List<String> missingModules = new ArrayList<>(0);
        for (String requiredName : module.requiredModules()) {
            if (loadedModules.containsKey(requiredName)) continue;
            Module required = pending.get(requiredName);
            if (required == null) {
                missingModules.add(requiredName);
            } else if (path.contains(required)) {
                List<String> loop = new ArrayList<>();
                for (Module looped : path.subList(path.indexOf(required), path.size())) {
                    loop.add(looped.getName());
                }
                loop.add(required.getName());
                platform.logger().warn(
                        "Found a loop in required modules: {}. Registering '{}' without waiting for '{}'",
                        String.join(" -> ", loop), module.getName(), required.getName()
                );
            } else if (!resolve(required, pending, path, failed)) {
                missingModules.add(requiredName);
            }
        }
        path.removeLast();
        boolean registered = false;
        if (missingModules.isEmpty()) {
            try {
                registered = register(module);
            } catch (Exception | LinkageError ex) {
                platform.logger().error("Failed to register '{}' module", module.getName(), ex);
            }
        } else {
            reportMissingModules(module, missingModules);
        }
        if (!registered) failed.add(module.getName());
        return registered;
    }

    private void reportMissingModules(@NotNull Module module, @NotNull List<String> missingModules) {
        if (module.isImportant()) {
            platform.logger().warn(
                    "Module '{}' cannot be registered because some of its required modules are missing: {}",
                    module.getName(), String.join(", ", missingModules)
            );
        }
    }

    @ApiStatus.Internal
    public void onEnable() {
        enabled = true;
        for (Listener listener : pendingListeners) {
            platform.getServer().getPluginManager().registerEvents(listener, platform.getPlugin());
        }
        pendingListeners.clear();
        for (Module module : loadedModules.values()) {
            module.onEnable(platform);
        }
    }

    @ApiStatus.Internal
    public void onDisable() {
        for (Module module : loadedModules.values()) {
            module.onDisable(platform);
        }
        enabled = false;
        for (URLClassLoader loader : classLoaders) {
            closeQuietly(loader);
        }
        classLoaders.clear();
    }

    public void registerModule(@NotNull Module module) {
        String name = module.getName();
        if (loadedModules.containsKey(name) || (later != null && later.containsKey(name))) {
            platform.logger().warn("Module '{}' is already registered", module.getName());
            return;
        }
        List<String> missingModules = new ArrayList<>(0);
        for (String requiredName : module.requiredModules()) {
            if (!loadedModules.containsKey(requiredName)) {
                missingModules.add(requiredName);
            }
        }
        if (later != null && (!module.requiredPlugins().isEmpty() || !missingModules.isEmpty())) {
            later.put(name, module);
        } else if (missingModules.isEmpty()) {
            register(module);
        } else {
            reportMissingModules(module, missingModules);
        }
    }

    private boolean register(@NotNull Module module) {
        if (module.getAuthors().isEmpty()) {
            platform.logger().info("Registering '{}' module", module.getName());
        } else {
            platform.logger().info("Registering '{}' module (by {})", module.getName(), String.join(", ", module.getAuthors()));
        }
        List<String> missingPlugins = checkPlugins(module);
        if (!missingPlugins.isEmpty()) {
            if (module.isImportant()) {
                platform.logger().warn(
                        "Module '{}' cannot be registered because some of its plugins are missing: {}",
                        module.getName(), String.join(", ", missingPlugins)
                );
            }
            return false;
        }
        module.onRegister(platform);
        if (module instanceof Module.WithActivatorTypes withTypes) {
            register("activators", withTypes.getActivatorTypes(), platform.getActivatorTypes()::registerType);
        }
        if (module instanceof Module.WithActions withActions) {
            register("actions", withActions.getActions(), platform.getActivities()::registerAction);
        }
        if (module instanceof Module.WithFlags withFlags) {
            register("flags", withFlags.getFlags(), platform.getActivities()::registerFlag);
        }
        if (module instanceof Module.WithPlaceholders withPlaceholders) {
            register("placeholders", withPlaceholders.getPlaceholders(), platform.getPlaceholders()::registerPlaceholder);
        }
        if (module instanceof Module.WithSelectors withSelectors) {
            register("selectors", withSelectors.getSelectors(), platform.getSelectors()::registerSelector);
        }
        if (module instanceof Module.WithAreas withAreas) {
            register("areas", withAreas.getAreas(), platform.getAreas()::registerArea);
        }
        if (module instanceof Module.WithItemAspects withItemAspects) {
            register("item aspects", withItemAspects.getItemAspects(), platform.getItems()::registerAspect);
        }
        registerHooks(module);
        loadedModules.put(module.getName(), module);
        if (enabled) {
            module.onEnable(platform);
        }
        return true;
    }

    private void registerHooks(@NotNull Object object) {
        if (object instanceof Reloadable reloadable) {
            platform.getConfiguration().register(reloadable);
        }
        if (object instanceof Listener listener) {
            if (enabled) {
                platform.getServer().getPluginManager().registerEvents(listener, platform.getPlugin());
            } else {
                pendingListeners.add(listener);
            }
        }
    }

    private @NotNull List<String> checkPlugins(@NotNull Module module) {
        List<String> missingPlugins = new ArrayList<>(0);
        PluginManager pluginManager = platform.getServer().getPluginManager();
        for (String pluginName : module.requiredPlugins()) {
            if (!pluginManager.isPluginEnabled(pluginName)) {
                missingPlugins.add(pluginName);
            }
        }
        return missingPlugins;
    }

    private <T extends Named> void register(String what, Collection<T> values, Consumer<T> register) {
        if (values.isEmpty()) return;
        List<String> names = new ArrayList<>(values.size());
        List<String> failed = null;
        for (T type : values) {
            try {
                register.accept(type);
                registerHooks(type);
                names.add(type.getName().toUpperCase(Locale.ROOT));
            } catch (Exception ex) {
                if (failed == null) failed = new ArrayList<>();
                failed.add(ex.getMessage());
            }
        }
        if (!names.isEmpty()) {
            debugInfo("Added " + names.size() + " " + what + ": " + String.join(", ", names));
        }
        if (failed != null && !failed.isEmpty()) {
            failed.forEach(platform.logger()::warn);
        }
    }

    @ApiStatus.Internal
    public void loadFolderModules() {
        if (loaded) throw new IllegalStateException("Modules from folder are already loaded");
        loaded = true;
        modulesFolder.mkdirs();
        ClassLoader parentLoader = Module.class.getClassLoader();
        for (File file : CollectionUtils.emptyOnNull(modulesFolder.listFiles((dir, name) -> name.endsWith(".jar")))) {
            URLClassLoader loader;
            try {
                loader = new URLClassLoader(new URL[]{file.toURI().toURL()}, parentLoader);
            } catch (MalformedURLException ex) {
                platform.logger().error("Invalid module jar path '{}'", file.getName(), ex);
                continue;
            }
            List<String> classNames;
            try {
                classNames = readModuleClassNames(loader);
            } catch (IOException ex) {
                platform.logger().error("Failed to read module jar '{}'", file.getName(), ex);
                closeQuietly(loader);
                continue;
            }
            if (classNames.isEmpty()) {
                platform.logger().warn("Module jar '{}' doesn't declare any modules in {}", file.getName(), SERVICES_PATH);
                closeQuietly(loader);
                continue;
            }
            classLoaders.add(loader);
            for (String className : classNames) {
                try {
                    Module module = Class.forName(className, true, loader)
                            .asSubclass(Module.class)
                            .getConstructor()
                            .newInstance();
                    registerModule(module);
                } catch (Exception | LinkageError ex) {
                    platform.logger().error("Failed to load module '{}' from '{}'", className, file.getName(), ex);
                }
            }
        }
    }

    private static @NotNull List<String> readModuleClassNames(@NotNull URLClassLoader loader) throws IOException {
        URL services = loader.findResource(SERVICES_PATH);
        if (services == null) return List.of();
        List<String> classNames = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(services.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int commentStart = line.indexOf('#');
                String className = (commentStart < 0 ? line : line.substring(0, commentStart)).strip();
                if (!className.isEmpty()) classNames.add(className);
            }
        }
        return classNames;
    }

    private void closeQuietly(@NotNull URLClassLoader loader) {
        try {
            loader.close();
        } catch (IOException ex) {
            platform.logger().warn("Failed to close module class loader", ex);
        }
    }

    private void debugInfo(String msg) {
        if (platform.getConfiguration().generalCfg().debugMode()) {
            platform.logger().info(msg);
        }
    }
}
