package me.fromgate.reactions.module;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.ActivatorType;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.selectors.Selector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ModulesRegistry {
    private final ReActions.Platform platform;
    private final File modulesFolder;
    private List<Module> later;
    private boolean loaded;

    public ModulesRegistry(@NotNull ReActions.Platform platform) {
        this.platform = platform;
        this.modulesFolder = new File(platform.getDataFolder(), "Modules");
        this.later = new ArrayList<>();
    }

    public void registerPluginDepended() {
        if (later == null) {
            throw new IllegalStateException("Plugin-depended modules are already registered.");
        }
        if (!later.isEmpty()) {
            platform.logger().info("Registering plugin-depended modules.");
            later.forEach(this::register);
        }
        later = null;
    }

    public void registerModule(@NotNull Module module) {
        if (module.isPluginDepended() && later != null) {
            later.add(module);
        } else {
            register(module);
        }
    }

    private void register(@NotNull Module module) {
        platform.logger().info("Registering " + module.getName() + " module (by " + String.join(", ", module.getAuthors()) + ")");
        if (!module.init(platform)) {
            return;
        }
        register("activators", module.getActivatorTypes(platform), ActivatorType::getName, platform.getActivatorTypes()::registerType);
        register("actions", module.getActions(platform), Action::getName, platform.getActivities()::registerAction);
        register("flags", module.getFlags(platform), Flag::getName, platform.getActivities()::registerFlag);
        register("placeholders", module.getPlaceholders(platform), Placeholder::getName, platform.getPlaceholders()::registerPlaceholder);
        register("selectors", module.getSelectors(platform), Selector::getName, platform.getSelectors()::registerSelector);
    }

    private <T> void register(String what, Collection<T> values, Function<T, String> toString, Consumer<T> register) {
        if (values.isEmpty()) return;
        List<String> names = new ArrayList<>(values.size());
        List<String> failed = null;
        for (T type : values) {
            try {
                register.accept(type);
                names.add(toString.apply(type).toUpperCase(Locale.ROOT));
            } catch (IllegalStateException e) {
                if (failed == null) failed = new ArrayList<>();
                failed.add(e.getMessage());
            }
        }
        if (!names.isEmpty()) {
            platform.logger().info("Registered " + names.size() + " " + what + ": " + String.join(", ", names));
        }
        if (failed != null && !failed.isEmpty()) {
            failed.forEach(platform.logger()::warn);
        }
    }

    public void loadFolderModules() {
        if (loaded) throw new IllegalStateException("Modules from folder are already loaded.");
        modulesFolder.mkdirs();
        List<Class<?>> toRegister = new ArrayList<>();
        ClassLoader moduleClassLoader = Module.class.getClassLoader();
        for (File file : modulesFolder.listFiles((dir, name) -> name.endsWith(".jar"))) {
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
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                break;
            }
        }
        for (Class<?> clazz : toRegister) {
            try {
                registerModule((Module) clazz.getConstructor().newInstance());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        loaded = true;
    }
}
