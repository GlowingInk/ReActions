package fun.reactions.util;

import fun.reactions.ReActions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class ConfigUtils {
    private ConfigUtils() {}

    @Contract(mutates = "param1")
    public static boolean loadConfig(@NotNull YamlConfiguration cfg, @NotNull File file, @NotNull String errorMsg) {
        try {
            if (!file.exists() && !file.createNewFile()) {
                throw new IOException("File doesn't exist and cannot be created");
            }
            cfg.load(file);
            return true;
        } catch (Exception ex) {
            ReActions.getLogger().error(errorMsg, ex);
            return false;
        }
    }

    public static boolean saveConfig(@NotNull YamlConfiguration cfg, @NotNull File file, @NotNull String errorMsg) {
        try {
            cfg.save(file);
            return true;
        } catch (Exception ex) {
            ReActions.getLogger().error(errorMsg, ex);
            return false;
        }
    }

    public static @NotNull ConfigurationSection getSection(@NotNull ConfigurationSection section, @NotNull String path) {
        //noinspection DataFlowIssue
        return section.isConfigurationSection(path)
                ? section.getConfigurationSection(path)
                : section.createSection(path);
    }
}
