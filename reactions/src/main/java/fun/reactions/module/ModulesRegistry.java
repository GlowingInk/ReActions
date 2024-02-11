package fun.reactions.module;

import fun.reactions.Cfg;
import fun.reactions.ReActions;
import fun.reactions.util.collections.CollectionUtils;
import fun.reactions.util.naming.Named;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ModulesRegistry {
    private final ReActions.Platform platform;
    private final File modulesFolder;
    private final List<Module> loadedModules;
    private List<Module> later;
    private boolean loaded;

    public ModulesRegistry(@NotNull ReActions.Platform platform) {
        this.platform = platform;
        this.modulesFolder = new File(platform.getDataFolder(), "Modules");
        this.later = new ArrayList<>();
        this.loadedModules = new ArrayList<>();
    }

    @ApiStatus.Internal
    public void registerPluginDepended() {
        if (later == null) {
            throw new IllegalStateException("Plugin-depended modules are already registered.");
        }
        if (!later.isEmpty()) {
            debugInfo("Registering plugin-depended modules.");
            later.forEach(this::register);
        }
        later = null;
    }

    @ApiStatus.Internal
    public void onDisable() {
        for (Module module : loadedModules) {
            module.onDisable(platform);
        }
    }

    public void registerModule(@NotNull Module module) {
        if (!module.requiredPlugins().isEmpty() && later != null) {
            later.add(module);
        } else {
            register(module);
        }
    }

    private void register(@NotNull Module module) {
        platform.logger().info("Registering '" + module.getName() + "' module (by " + String.join(", ", module.getAuthors()) + ")");
        List<String> missingPlugins = checkPlugins(module);
        if (!missingPlugins.isEmpty()) {
            platform.logger().warn("Module '" + module.getName() + "' cannot be registered because some plugins are missing: " + String.join(", ", missingPlugins));
            return;
        }
        module.preRegister(platform);
        register("activators", module.getActivatorTypes(), platform.getActivatorTypes()::registerType);
        register("actions", module.getActions(), platform.getActivities()::registerAction);
        register("flags", module.getFlags(), platform.getActivities()::registerFlag);
        register("placeholders", module.getPlaceholders(), platform.getPlaceholders()::registerPlaceholder);
        register("selectors", module.getSelectors(), platform.getSelectors()::registerSelector);
        module.postRegister(platform);
        loadedModules.add(module);
    }

    private @NotNull List<String> checkPlugins(@NotNull Module module) {
        List<String> missingPlugins = new ArrayList<>(0);
        PluginManager pluginManager = platform.getServer().getPluginManager();
        for (String str : module.requiredPlugins()) {
            if (!pluginManager.isPluginEnabled(str)) {
                missingPlugins.add(str);
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

    public void loadFolderModules() {
        if (loaded) throw new IllegalStateException("Modules from folder are already loaded");
        modulesFolder.mkdirs();
        List<Class<?>> toRegister = new ArrayList<>();
        ClassLoader moduleClassLoader = Module.class.getClassLoader();
        for (File file : CollectionUtils.emptyOnNull(modulesFolder.listFiles((dir, name) -> name.endsWith(".jar")))) {
            URI fileUri = file.toURI();
            try (JarInputStream stream = new JarInputStream(fileUri.toURL().openStream());
                URLClassLoader loader = new URLClassLoader(new URL[]{fileUri.toURL()}, moduleClassLoader)) {
                JarEntry entry;
                while ((entry = stream.getNextJarEntry()) != null) {
                    String name = entry.getName();
                    if (!name.endsWith(".class")) {
                        continue;
                    }
                    Class<?> clazz = loader.loadClass(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
                    if (Module.class.isAssignableFrom(clazz)) {
                        toRegister.add(clazz);
                    }
                }
            } catch (Exception ex) {
                platform.logger().error("Something went wrong during module parsing", ex);
            }
        }
        for (Class<?> clazz : toRegister) {
            try {
                registerModule((Module) clazz.getConstructor().newInstance());
            } catch (Exception ex) {
                platform.logger().error("Something went wrong during module registration", ex);
            }
        }
        loaded = true;
    }

    private void debugInfo(String msg) {
        if (Cfg.debugMode) {
            platform.logger().info(msg);
        }
    }
}
