package fun.reactions.util;

import fun.reactions.ReActions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public final class FileUtils {
    private FileUtils() {}

    public static boolean loadCfg(@NotNull YamlConfiguration cfg, @NotNull File file, @NotNull String errorMsg) {
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

    public static boolean saveCfg(@NotNull YamlConfiguration cfg, @NotNull File file, @NotNull String errorMsg) {
        try {
            cfg.save(file);
            return true;
        } catch (Exception ex) {
            ReActions.getLogger().error(errorMsg, ex);
            return false;
        }
    }
}
