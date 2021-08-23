package me.fromgate.reactions.module;

import me.fromgate.reactions.ReActions;
import me.fromgate.reactions.selectors.SelectorsManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ModulesManager {
    private final ReActions.Platform platform;
    private final ClassLoader classLoader;
    private final File folder;

    public ModulesManager(ReActions.Platform platform, File folder, ClassLoader classLoader) {
        this.platform = platform;
        this.folder = new File(folder, File.separator + "modules");
        this.classLoader = classLoader;
        loadModules();
    }

    private void loadModules() {
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
                            loadModule((Module) clazz.getConstructor().newInstance());
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
    }

    private void loadModule(Module module) {
        // TODO: Count loaded
        module.getActions().forEach(platform.getActivities()::registerAction);
        module.getFlags().forEach(platform.getActivities()::registerFlag);
        module.getActivatorTypes().forEach(platform.getActivators()::registerType);
        module.getPlaceholders().forEach(platform.getPlaceholders()::register);
        module.getSelectors().forEach(SelectorsManager::addSelector);
        platform.getLogger().info("Loaded module " + module.getName() + " (" + String.join(", ", module.getAuthors()) + ")");
    }
}
