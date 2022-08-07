package me.fromgate.reactions.module;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.logic.activators.ActivatorType;
import me.fromgate.reactions.logic.activity.actions.Action;
import me.fromgate.reactions.logic.activity.flags.Flag;
import me.fromgate.reactions.placeholders.Placeholder;
import me.fromgate.reactions.selectors.Selector;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ModulesManager {
    private final ReActions.Platform platform;
    private final ClassLoader classLoader;
    private final File folder;

    private boolean loaded;

    public ModulesManager(ReActions.Platform platform, ClassLoader classLoader) {
        this.platform = platform;
        this.folder = new File(platform.getPlugin().getDataFolder(), File.separator + "modules");
        this.classLoader = classLoader;
    }

    public void registerModule(Module module) {
        platform.getLogger().info("Registering " + module.getName() + " module (by " + StringUtils.join(module.getAuthors(), ", ") + ")");
        register("activators", module.getActivatorTypes(platform), ActivatorType::getName, platform.getActivators()::registerType);
        register("actions", module.getActions(platform), Action::getName, platform.getActivities()::registerAction);
        register("flags", module.getFlags(platform), Flag::getName, platform.getActivities()::registerFlag);
        register("placeholders", module.getPlaceholders(platform), Placeholder::getBasicName, platform.getPlaceholders()::registerPlaceholder);
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
            platform.getLogger().info("Registered " + names.size() + " " + what + ": " + String.join(", ", names));
        }
        if (failed != null && !failed.isEmpty()) {
            failed.forEach(platform.getLogger()::warning);
        }
    }

    public void loadModules() {
        // TODO
        if (loaded) throw new IllegalStateException("Modules from folder are already loaded!");
        folder.mkdirs();
        for (File file : folder.listFiles()) {
            if (!file.getName().endsWith(".jar")) {
                platform.getLogger().warning(file.getName() + " is not a jar archive - skipping it.");
                continue;
            }
            try (final JarInputStream stream = new JarInputStream(file.toURI().toURL().openStream())) {
                JarEntry entry;
                while ((entry = stream.getNextJarEntry()) != null) {
                    final String name = entry.getName();
                    if (!name.endsWith(".class")) {
                        continue;
                    }
                    try {
                        Class<?> clazz = classLoader.loadClass(name.substring(0, name.lastIndexOf('.')).replace('/', '.'));
                        if (Module.class.isAssignableFrom(clazz)) {
                            registerModule((Module) clazz.getConstructor().newInstance());
                            break;
                        }
                    } catch (NoClassDefFoundError | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        // TODO: Properly log errors
                        e.printStackTrace();
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        loaded = true;
    }
}
