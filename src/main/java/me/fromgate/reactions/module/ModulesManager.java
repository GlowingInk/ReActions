package me.fromgate.reactions.module;

import me.fromgate.reactions.ReActions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
        // TODO: Count loaded
        module.getActions().forEach(platform.getActivities()::registerAction);
        module.getFlags().forEach(platform.getActivities()::registerFlag);
        module.getActivatorTypes().forEach(platform.getActivators()::registerType);
        module.getPlaceholders().forEach(platform.getPlaceholders()::registerPlaceholder);
        module.getSelectors().forEach(platform.getSelectors()::registerSelector);
        platform.getLogger().info("Loaded module " + module.getName() + " (" + String.join(", ", module.getAuthors()) + ")");
    }

    public void loadModules() {
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
